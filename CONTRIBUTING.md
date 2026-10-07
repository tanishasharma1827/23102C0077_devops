# Contributing Guide

## Branches

| Branch | Purpose |
|--------|---------|
| `main` | Stable, release-ready code. Only updated by merging `develop` through a pull request. |
| `develop` | Integration branch. All features are merged here first. |
| `feature/<id>-<short-name>` | A new feature, e.g. `feature/M4-user-registration` |
| `bugfix/<id>-<short-name>` | A bug fix, e.g. `bugfix/M10-double-booking` |
| `hotfix/<short-name>` | Urgent fix for `main` |
| `release/<version>` | Release preparation, e.g. `release/v1.0.0` |

Rules:
- Use lowercase and hyphens only in branch names.
- Never commit directly to `main` or `develop`.
- One branch = one backlog item / issue.

## Workflow

```bash
git checkout develop
git pull
git checkout -b feature/M4-user-registration
# ...make changes...
git add .
git commit -m "feat: add user registration form"
git push -u origin feature/M4-user-registration
# open a Pull Request into develop on GitHub
```

## Commit messages

Format: `type: short description` (present tense, under 72 characters)

Types: `feat`, `fix`, `docs`, `test`, `chore`, `ci`, `refactor`

Add `Closes #<issue-number>` in the PR description to auto-close the issue.

## Before opening a Pull Request

- `mvn clean package` passes
- The PR template checklist is completed
