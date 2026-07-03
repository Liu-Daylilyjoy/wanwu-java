# File Upload Compatibility Reproduction

Date: 2026-06-30

## Go Source Baseline

The Go BFF registers shared file routes in `v1/common.go`:

- `GET /file/check`
- `GET /file/check/list`
- `POST /file/upload`
- `POST /file/upload/direct`
- `POST /file/merge`
- `POST /file/clean`
- `DELETE /file/delete`

The public OpenURL router also exposes anonymous chunk upload routes:

- `POST /file/upload`
- `POST /file/merge`
- `POST /file/clean`

The unchanged frontend calls these through:

- `/service/api/v1/file/*` from `web/src/api/chunkFile.js`.
- `/service/url/openurl/v1/file/*` for public web-chat uploads.
- `/service/api/v1/proxy/file/upload` from workflow external upload helpers.
- `/service/api/v1/inferpub/upload` from the workflow import upload dialog action.

## Java Reproduction

`WanwuFileApiController` adds a BFF-local compatibility implementation:

- Chunk upload writes pieces to `${java.io.tmpdir}/wanwu-java-uploads/chunks/{md5(chunkName)}/upload`.
- Merge validates chunk count and optional file size, then writes a merged file under `${java.io.tmpdir}/wanwu-java-uploads/files`.
- Direct upload returns the Go frontend shape `files[{fileName,fileId,filePath,fileSize}]`.
- Merge returns `originalFileName`, stored `fileName`, and frontend-reachable `filePath`.
- Delete removes stored local files by file id or returned download path.
- OpenURL aliases reuse the same chunk upload, merge, and clean behavior.
- Docker nginx now proxies `/service/api/` to the Java BFF.

## Current Boundary

This is a development upload loop, not the final Go object storage reproduction:

- Files are local to the BFF container and disappear when the container filesystem is replaced.
- There is no MinIO bucket, signed URL, expiration policy, malware scan, or cross-service file metadata table yet.
- Knowledge document parsing, Safety Guard sensitive-word imports, RAG ingestion, and workflow runtime file semantics remain separate business slices.

## Verification

- `WanwuFileApiControllerTest` covers chunk check/upload/list/merge/download/clean/delete, direct upload, OpenURL upload, proxy upload, and inferpub upload response shapes.
- Docker Compose smoke should verify `/service/api/v1/file/upload/direct` through `http://localhost:3000` after rebuilding both BFF and Web.
