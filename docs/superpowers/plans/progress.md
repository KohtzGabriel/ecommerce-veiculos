# SDD ledger — plan: docs/superpowers/plans/2026-10-02-cadastro-cliente-cep.md

Ruling: the current workspace is not a Git checkout, so implementation will use the workspace directly with `backend/` copied from the professor reference and `frontend/` as a new Angular app; the reference clone remains unchanged and is read-only.

Pre-flight: Task 1 produces the API contracts consumed by Tasks 2–4; Task 2 produces the structured address and CEP contracts consumed by Task 3 and Task 4; Task 3 produces the client endpoint consumed by Task 4. No conflicting interfaces found.

Task 1: Ruling: Angular CLI 21.2.24 accepts `--ai-config=agents`, not the plan's `--ai-config=agent`; use the installed CLI's documented choice because it is the same requested Angular agent configuration. Cost if wrong: only the generated AI configuration selection changes.

Task 1 completed: backend copied from the professor reference, Angular 21.2.24 scaffolded, Java 25 user `JAVA_HOME`/`Path` configured, and API contracts added.

Task 2 completed: structured `Endereco`/`Municipio`/`Estado` persistence added; ViaCEP integration is isolated behind a REST client and returns a controlled 502 when unavailable; municipality name matching ignores accents.

Task 3 completed: `POST /clientes` validates the client and nested address, checks CPF/e-mail uniqueness, resolves the municipality, and persists the client with a structured address.

Task 4 completed: Angular reactive form added with front-end validation, CEP lookup on blur/button, successful patching, manual municipality selection fallback, and client submission.

Task 5 completed: README added; backend tests use H2 only in the test profile; full backend and frontend verification is pending final fresh run.

Final verification completed: `backend/mvnw.cmd test` passed with 23 tests; `frontend/npm test -- --watch=false` passed with 2 test files and 2 tests; `frontend/npm run build` completed successfully.

# SDD ledger — plan: docs/superpowers/plans/2026-10-16-auth-nf-veiculos.md

Tasks 1–7 completed: JWT authentication for admin/customer; customer CPF and vehicle filter queries; invoice entrance creates vehicles; invoice exit marks vehicles sold; invoice DELETE reverses stock safely; Angular role-specific login/session guards; read-only storefront.

Final verification: `backend/mvnw.cmd test -q` exited 0 (41 tests across 13 test classes); `frontend/npm test -- --watch=false` passed (8 files, 17 tests); `frontend/npm run build` exited 0. Catalog-only tests also passed (3 tests). New-vehicle request validation now matches persistent column limits (description 500, model 100, color 40).

Debugging note: corrected the mismatched-role test fixture, which had returned CLIENTE while expecting CLIENTE and therefore was not actually exercising rejection. No production code change was needed for that test failure.
