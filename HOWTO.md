# How to contribute

## How to merge changes

1. **Never push directly to `main`.** Create a branch from main:
   `git checkout -b [feature/branchname]`
2. Make your changes **and add unit tests** in `app/src/test/`.
3. Push your branch and open a **pull request** into `main`.
4. Fill in the PR checklist below.
5. Ask a teammate to review. **The reviewer must not be the person who wrote the code.**
6. Merge only when CI is green and the PR is approved.

## PR checklist

### Author
- [ ] Branch is up to date with `main`
- [ ] New/changed logic has unit tests
- [ ] All tests pass locally
- [ ] No secrets, API keys or passwords in the code
- [ ] AI used? → Logged in the AI dossier (tool, task, prompt, verification, decision)
- [ ] AI output was wrong/off-spec? → Added to the failure log
- [ ] I can explain every line in this PR
- [ ] Updated `CLAUDE.md` if the agent got a convention wrong

### Reviewer
- [ ] I did not write or generate this code
- [ ] Tests actually check the behavior (not just that it runs)
- [ ] Code follows the conventions in `CLAUDE.md`
- [ ] CI is green