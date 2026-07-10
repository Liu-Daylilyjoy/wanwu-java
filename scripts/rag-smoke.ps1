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
$knowledgeId = $null
$ragId = $null
$nonce = [DateTimeOffset]::UtcNow.ToUnixTimeMilliseconds()
$fact = "Wanwu RAG smoke fact ${nonce}: the verified retention period is 37 days."

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
        $parameters.Body = $Body | ConvertTo-Json -Depth 20
    }
    $response = Invoke-RestMethod @parameters
    if ($null -ne $response.code -and [int]$response.code -ne 0) {
        throw "Wanwu request failed: $Method $Path, code=$($response.code), message=$($response.message)"
    }
    return $response
}

try {
    $health = Invoke-RestMethod -Method Get -Uri "$BaseUrl/actuator/health"
    if ($health.status -ne "UP") {
        throw "BFF health is $($health.status)"
    }

    $knowledge = Invoke-WanwuJson -Method Post -Path "/user/api/v1/knowledge" -Body @{
        name = "RAG Smoke KB $nonce"
        description = "Disposable end-to-end RAG acceptance knowledge base"
        category = 0
        avatar = @{ path = "" }
        knowledgeGraph = @{ switch = $true }
    }
    $knowledgeId = [string]$knowledge.data.knowledgeId
    if ([string]::IsNullOrWhiteSpace($knowledgeId)) {
        throw "Knowledge creation did not return knowledgeId"
    }

    Invoke-WanwuJson -Method Post -Path "/user/api/v1/knowledge/doc/import" -Body @{
        knowledgeId = $knowledgeId
        docInfoList = @(@{
            docId = "rag-smoke-doc-$nonce"
            docName = "rag-smoke-$nonce.txt"
            docType = "txt"
            docSize = $fact.Length
            content = $fact
        })
        docSegment = @{
            segmentMethod = "0"
            segmentType = "0"
            maxSplitter = 500
        }
        docAnalyzer = @("text")
    } | Out-Null

    $rag = Invoke-WanwuJson -Method Post -Path "/user/api/v1/appspace/rag" -Body @{
        name = "RAG Smoke App $nonce"
        desc = "Disposable end-to-end RAG acceptance app"
        avatar = @{ key = ""; path = "" }
    }
    $ragId = [string]$rag.data.ragId
    if ([string]::IsNullOrWhiteSpace($ragId)) {
        throw "RAG creation did not return ragId"
    }

    Invoke-WanwuJson -Method Put -Path "/user/api/v1/appspace/rag/config" -Body @{
        ragId = $ragId
        knowledgeBaseConfig = @{
            knowledgebases = @(@{
                id = $knowledgeId
                name = "RAG Smoke KB $nonce"
                graphSwitch = 1
            })
            config = @{
                maxHistory = 1
                threshold = 0.1
                topK = 5
                matchType = "mix"
                priorityMatch = 1
                semanticsPriority = 0.2
                keywordPriority = 0.8
                useGraph = $true
            }
        }
    } | Out-Null

    $chatBody = @{
        ragId = $ragId
        question = "What is the verified retention period?"
        history = @(@{
            query = "Which acceptance run is this?"
            response = "It is run $nonce."
            needHistory = $true
        })
        fileInfo = @()
    } | ConvertTo-Json -Depth 20
    $chatHeaders = @{
        Authorization = "Bearer $Token"
        Accept = "text/event-stream"
    }
    $chat = Invoke-WebRequest -Method Post -Uri "$BaseUrl/user/api/v1/rag/chat/draft" `
        -Headers $chatHeaders -ContentType "application/json" -Body $chatBody -UseBasicParsing
    if ($chat.Content -notmatch [regex]::Escape("37 days")) {
        throw "RAG SSE response did not contain the indexed fact"
    }
    if ($chat.Content -notmatch "rag_search_list") {
        throw "RAG SSE response did not contain the knowledge search-list event"
    }

    Write-Host "RAG smoke passed: knowledgeId=$knowledgeId ragId=$ragId"
}
finally {
    if (-not $KeepData) {
        if (-not [string]::IsNullOrWhiteSpace($ragId)) {
            Invoke-WanwuJson -Method Delete -Path "/user/api/v1/appspace/rag" -Body @{ ragId = $ragId } | Out-Null
        }
        if (-not [string]::IsNullOrWhiteSpace($knowledgeId)) {
            Invoke-WanwuJson -Method Delete -Path "/user/api/v1/knowledge" -Body @{ knowledgeId = $knowledgeId } | Out-Null
        }
    }
}
