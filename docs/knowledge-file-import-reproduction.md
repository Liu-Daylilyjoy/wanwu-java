# Knowledge File Import Reproduction

Date: 2026-07-05

## Go Source Baseline

Checked source:

- `D:\work\week3\wanwu\internal\bff-service\server\http\handler\v1\knowledge_doc.go`
- `D:\work\week3\wanwu\internal\knowledge-service\client\model\knowledge_doc.go`
- `D:\work\week3\wanwu\internal\knowledge-service\client\orm\knowledge_import_task.go`

The Go backend stores document metadata, file path, parser/indexer status, and async import task state. Real file conversion/indexing is done through the knowledge service task pipeline and downstream parser/RAG services.

## Java Development Parity

The Java knowledge service now keeps the existing synchronous development import loop, but document import can extract text from base64 `.xlsx`, `.docx`, and `.pdf` uploads before segmenting:

- Inline `content` / `text` / `docContent` still goes straight into the existing splitter.
- Base64 text files still decode as UTF-8.
- Base64 `.xlsx` files are parsed locally from `xl/worksheets/sheet*.xml` and `xl/sharedStrings.xml`, then converted to tab/newline-delimited text before segmentation.
- Base64 `.docx` files are parsed locally from `word/document.xml`, joining Word paragraph text before segmentation.
- Base64 `.pdf` files are parsed with Apache PDFBox 2.x before segmentation.
- Reimport keeps using the captured source content, so updated split settings can rebuild segments from the parsed spreadsheet text.

This covers the frontend's development-time spreadsheet, Word, and PDF document import without an external parser service.

## Remaining Gap

Legacy DOC, MinIO object lifecycle, parser model execution, async task state, vector indexing, and Go-equivalent normalized import tables remain later slices.
