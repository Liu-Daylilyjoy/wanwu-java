# Safety MySQL Persistence

Date: 2026-06-30

## Go Baseline

Checked from:

- `D:\work\week3\wanwu\internal\app-service\client\model\safety.go`
- `D:\work\week3\wanwu\internal\app-service\client\orm\safety.go`
- `D:\work\week3\wanwu\internal\app-service\server\grpc\safety\service.go`
- `D:\work\week3\wanwu\proto\safety-service\safety-service.proto`

The Go app-service persists safety data in MySQL-backed GORM models:

- `SensitiveWordTable`: user/org scoped table metadata, reply text, version, and table type.
- `SensitiveWordVocabulary`: table-scoped sensitive words with sensitive type.

Go updates the table version when reply or vocabulary contents change, caps a table at 100 words, rejects duplicate single-word uploads, and deletes table words in the same transaction when a table is removed.

## Java Coverage

The Java app service now adds `app_service.safety_records` as a Docker MySQL compatibility table. `SafetyServiceImpl` loads a single `snapshot/state` record at startup and saves it after every mutable Safety Guard operation:

- sensitive table create/update/reply/delete
- sensitive word single upload/delete
- personal table selector used by Agent/RAG safety configuration

The snapshot stores table metadata, table words, and the next word sequence so Docker restarts do not reset word IDs. Java also now keeps the Go-compatible table `version`, duplicate single-word guard, and 100-word table cap.

## Verification

- Targeted Maven test: `mvn -q -pl wanwu-service-app -am "-Dtest=SafetyServiceImplTest" -DfailIfNoTests=false test`

## Remaining Gaps

- Normalize the snapshot into Go-equivalent `sensitive_word_tables` and `sensitive_word_vocabularies` tables.
- Parse Excel sensitive-word uploads from object storage.
- Apply Aho-Corasick sensitive-word interception to Agent/RAG/Model streaming runtimes.
