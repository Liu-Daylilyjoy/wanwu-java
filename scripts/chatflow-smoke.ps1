param(
    [string]$BaseUrl = "http://127.0.0.1:8080",
    [string]$Token = "dev-token",
    [switch]$KeepData
)

$ErrorActionPreference = "Stop"
$headers = @{
    Authorization = "Bearer $Token"
    Accept = "application/json"
}
$chatflowId = $null
$conversationId = $null
$inputChatflowId = $null
$inputConversationId = $null
$nonce = [DateTimeOffset]::UtcNow.ToUnixTimeMilliseconds()
$question = "Chatflow Docker smoke $nonce"

function Invoke-WanwuJson {
    param(
        [string]$Method,
        [string]$Path,
        [object]$Body
    )

    $parameters = @{
        Method = $Method
        Uri = "$BaseUrl$Path"
        Headers = $headers
        ContentType = "application/json"
    }
    if ($null -ne $Body) {
        $parameters.Body = $Body | ConvertTo-Json -Depth 30
    }
    $response = Invoke-RestMethod @parameters
    if ($null -ne $response.code -and [int]$response.code -ne 0) {
        throw "Wanwu request failed: $Method $Path, code=$($response.code), message=$($response.message)"
    }
    return $response
}

$schema = @{
    nodes = @(
        @{
            id = "100001"
            type = "1"
            data = @{
                nodeMeta = @{ title = "Start" }
                outputs = @(@{ type = "string"; name = "input"; required = $true })
            }
        },
        @{
            id = "900001"
            type = "2"
            data = @{
                nodeMeta = @{ title = "End" }
                inputs = @{
                    inputParameters = @(@{
                        name = "output"
                        input = @{
                            type = "string"
                            value = @{
                                type = "ref"
                                content = @{ source = "block-output"; blockID = "100001"; name = "input" }
                            }
                        }
                    })
                }
            }
        }
    )
    edges = @(@{ sourceNodeID = "100001"; targetNodeID = "900001" })
} | ConvertTo-Json -Depth 30 -Compress

$inputSchema = @{
    nodes = @(
        @{
            id = "100001"
            type = "1"
            data = @{
                nodeMeta = @{ title = "Start" }
                outputs = @(@{ type = "string"; name = "input"; required = $true })
            }
        },
        @{
            id = "input-1"
            type = "30"
            data = @{
                nodeMeta = @{ title = "Location" }
                outputs = @(@{
                    type = "string"
                    name = "city"
                    required = $true
                    description = "City for policy lookup"
                })
                inputs = @{
                    inputParameters = $null
                    outputSchema = '[{"type":"string","name":"city","required":true}]'
                }
            }
        },
        @{
            id = "900001"
            type = "2"
            data = @{
                nodeMeta = @{ title = "End" }
                inputs = @{
                    inputParameters = @(@{
                        name = "output"
                        input = @{
                            type = "string"
                            value = @{
                                type = "ref"
                                content = @{ source = "block-output"; blockID = "input-1"; name = "city" }
                            }
                        }
                    })
                }
            }
        }
    )
    edges = @(
        @{ sourceNodeID = "100001"; targetNodeID = "input-1" },
        @{ sourceNodeID = "input-1"; targetNodeID = "900001" }
    )
} | ConvertTo-Json -Depth 30 -Compress

try {
    $health = Invoke-RestMethod -Method Get -Uri "$BaseUrl/actuator/health"
    if ($health.status -ne "UP") {
        throw "BFF health is $($health.status)"
    }

    $created = Invoke-WanwuJson -Method Post -Path "/user/api/v1/appspace/chatflow" -Body @{
        name = "Chatflow Smoke $nonce"
        desc = "Disposable Docker Chatflow acceptance app"
        schema = $schema
    }
    $chatflowId = [string]$created.data.workflowId
    if ([string]::IsNullOrWhiteSpace($chatflowId)) {
        $chatflowId = [string]$created.data.workflow_id
    }
    if ([string]::IsNullOrWhiteSpace($chatflowId)) {
        throw "Chatflow creation did not return workflowId"
    }

    $conversation = Invoke-WanwuJson -Method Post -Path "/service/api/openapi/v1/chatflow/conversation" -Body @{
        uuid = $chatflowId
        conversation_name = "Smoke conversation $nonce"
    }
    $conversationId = [string]$conversation.data.conversation_id
    if ([string]::IsNullOrWhiteSpace($conversationId)) {
        throw "Chatflow conversation creation did not return conversation_id"
    }

    $chatResponse = Invoke-WebRequest -Method Post -Uri "$BaseUrl/service/api/openapi/v1/chatflow/chat" `
        -Headers @{ Authorization = "Bearer $Token"; Accept = "text/event-stream" } `
        -ContentType "application/json" -Body (@{
            uuid = $chatflowId
            conversation_id = $conversationId
            query = $question
            parameters = @{ acceptance = "docker" }
        } | ConvertTo-Json -Depth 20) -UseBasicParsing

    $events = @(
        "event: conversation.chat.created",
        "event: conversation.chat.in_progress",
        "event: conversation.message.delta",
        "event: conversation.message.completed",
        "event: conversation.chat.completed",
        "event: done"
    )
    $previousIndex = -1
    foreach ($event in $events) {
        $index = $chatResponse.Content.IndexOf($event)
        if ($index -lt 0 -or $index -le $previousIndex) {
            throw "Chatflow SSE event missing or out of order: $event"
        }
        $previousIndex = $index
    }
    if ($chatResponse.Content -notmatch [regex]::Escape($question)) {
        throw "Chatflow SSE response did not contain the graph result"
    }

    $messages = Invoke-WanwuJson -Method Post -Path "/service/api/openapi/v1/chatflow/conversation/message/list" -Body @{
        uuid = $chatflowId
        conversation_id = $conversationId
        limit = 10
    }
    if ($messages.data.data.Count -ne 2) {
        throw "Chatflow history expected 2 messages, got $($messages.data.data.Count)"
    }
    if ($messages.data.data[0].content -ne $question -or $messages.data.data[1].content -ne $question) {
        throw "Chatflow history did not persist the user and assistant turn"
    }

    $inputCreated = Invoke-WanwuJson -Method Post -Path "/user/api/v1/appspace/chatflow" -Body @{
        name = "Chatflow Input Smoke $nonce"
        desc = "Disposable Chatflow input resume acceptance app"
        schema = $inputSchema
    }
    $inputChatflowId = [string]$inputCreated.data.workflowId
    if ([string]::IsNullOrWhiteSpace($inputChatflowId)) {
        $inputChatflowId = [string]$inputCreated.data.workflow_id
    }
    $inputConversation = Invoke-WanwuJson -Method Post -Path "/service/api/openapi/v1/chatflow/conversation" -Body @{
        uuid = $inputChatflowId
        conversation_name = "Input smoke conversation $nonce"
    }
    $inputConversationId = [string]$inputConversation.data.conversation_id

    $waitingResponse = Invoke-WebRequest -Method Post -Uri "$BaseUrl/service/api/openapi/v1/chatflow/chat" `
        -Headers @{ Authorization = "Bearer $Token"; Accept = "text/event-stream" } `
        -ContentType "application/json" -Body (@{
            uuid = $inputChatflowId
            conversation_id = $inputConversationId
            query = "Which local policy applies?"
            parameters = @{}
        } | ConvertTo-Json -Depth 20) -UseBasicParsing
    if ($waitingResponse.Content -notmatch '"finish":0' `
            -or $waitingResponse.Content -notmatch '"type":"question"' `
            -or $waitingResponse.Content -notmatch '"node_id":"input-1"') {
        throw "Chatflow input node did not emit the waiting-input SSE contract"
    }

    $resumedResponse = Invoke-WebRequest -Method Post -Uri "$BaseUrl/service/api/openapi/v1/chatflow/chat" `
        -Headers @{ Authorization = "Bearer $Token"; Accept = "text/event-stream" } `
        -ContentType "application/json" -Body (@{
            uuid = $inputChatflowId
            conversation_id = $inputConversationId
            query = "Shanghai"
            parameters = @{}
        } | ConvertTo-Json -Depth 20) -UseBasicParsing
    if ($resumedResponse.Content -notmatch '"finish":1' `
            -or $resumedResponse.Content -notmatch 'Shanghai') {
        throw "Chatflow input node did not resume with the supplied answer"
    }

    $inputMessages = Invoke-WanwuJson -Method Post -Path "/service/api/openapi/v1/chatflow/conversation/message/list" -Body @{
        uuid = $inputChatflowId
        conversation_id = $inputConversationId
        limit = 10
    }
    if ($inputMessages.data.data.Count -ne 4 `
            -or $inputMessages.data.data[1].type -ne "question" `
            -or $inputMessages.data.data[3].content -ne "Shanghai") {
        throw "Chatflow input resume history was not persisted correctly"
    }

    Write-Host "Chatflow smoke passed: chatflowId=$chatflowId inputChatflowId=$inputChatflowId"
}
finally {
    if (-not $KeepData) {
        if (-not [string]::IsNullOrWhiteSpace($inputConversationId)) {
            Invoke-WanwuJson -Method Delete -Path "/service/api/openapi/v1/chatflow/conversation" -Body @{
                uuid = $inputChatflowId
                conversation_id = $inputConversationId
            } | Out-Null
        }
        if (-not [string]::IsNullOrWhiteSpace($inputChatflowId)) {
            Invoke-WanwuJson -Method Delete -Path "/user/api/v1/appspace/app" -Body @{
                appId = $inputChatflowId
                appType = "chatflow"
            } | Out-Null
        }
        if (-not [string]::IsNullOrWhiteSpace($conversationId)) {
            Invoke-WanwuJson -Method Delete -Path "/service/api/openapi/v1/chatflow/conversation" -Body @{
                uuid = $chatflowId
                conversation_id = $conversationId
            } | Out-Null
        }
        if (-not [string]::IsNullOrWhiteSpace($chatflowId)) {
            Invoke-WanwuJson -Method Delete -Path "/user/api/v1/appspace/app" -Body @{
                appId = $chatflowId
                appType = "chatflow"
            } | Out-Null
        }
    }
}
