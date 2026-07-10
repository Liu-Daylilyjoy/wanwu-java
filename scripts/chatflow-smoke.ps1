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

    Write-Host "Chatflow smoke passed: chatflowId=$chatflowId conversationId=$conversationId"
}
finally {
    if (-not $KeepData) {
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
