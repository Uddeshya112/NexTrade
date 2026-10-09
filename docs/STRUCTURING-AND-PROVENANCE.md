# Structuring / Provenance Notes

## Source
Input file: `docs/input/Pasted-text-20261008-214506.txt`

The current pasted source declares 56 file paths. Of those, 52 contain concrete fenced code blocks in the supplied paste and were written to their exact declared paths.

The paste explicitly says "Already provided above - complete implementation" for these four paths:
- `nextrade-api/src/main/java/com/nextrade/api/websocket/WebSocketConfig.java`
- `nextrade-api/src/main/java/com/nextrade/api/websocket/interceptor/JwtChannelInterceptor.java`
- `nextrade-fx-client/src/main/java/com/nextrade/client/security/TokenStorage.java`
- `nextrade-fx-client/src/main/java/com/nextrade/client/service/ApiClient.java`

For those four only, the implementation was copied from the immediately preceding NexTrade package and placed at the exact path declared by the current paste. The prior duplicate path for `JwtChannelInterceptor.java` was removed so the assembled tree contains only the current declared location.

The remaining modules/files come from the preceding NexTrade project base because the current paste does not contain their code blocks. This allows one complete project tree while preserving provenance.

## Counts
- Combined project files: 690
- Java files: 657
- Java source lines: 28,116
- Concrete files from current paste: 52
- Lines across concrete current-paste blocks: 3,323

## Important
This artifact is a **structuring/assembly deliverable**. It does not mean the supplied source has been independently corrected or fully build-verified. The supplied snippets themselves contain unresolved inconsistencies; they are preserved where they came from rather than silently altered.

Use `STRUCTURE.md` for the exact project tree and `FILE-MANIFEST.md` for provenance.
