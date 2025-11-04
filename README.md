# Assignment 5 – Continuous Integration and Automated Testing

![SE333_CI](https://github.com/rsahoo968/Assignment5_Code/actions/workflows/SE333_CI.yml/badge.svg)

**Workflow Link:** [View SE333_CI Workflow on GitHub](https://github.com/rsahoo968/Assignment5_Code/actions/workflows/SE333_CI.yml)

---

## Project Overview
This repository was created for **SE333 – Software Testing and Continuous Integration**.  
The purpose of this project is to design, test, and automate a complete CI pipeline that ensures every code change is analyzed, tested, and verified automatically using **GitHub Actions**.

The project includes:
- **Part 1:** BarnesAndNoble Testing (Specification and Structural Testing)
- **Part 2:** Continuous Integration setup with Checkstyle and JaCoCo
- **Part 3:** Amazon Unit and Integration Testing

Each commit triggers the CI pipeline to:
1. Run **Checkstyle** for static code analysis.
2. Execute all **JUnit 5** tests automatically.
3. Generate **JaCoCo** test coverage reports.
4. Upload reports (`checkstyle.xml` and `jacoco.xml`) as GitHub Action artifacts.

---

## Continuous Integration Workflow

**Workflow File:** `.github/workflows/SE333_CI.yml`  
**Workflow Name:** `SE333_CI`

### Trigger
- The workflow runs automatically on every push to the `main` branch.
- It uses the **Ubuntu-latest** GitHub runner.

### Environment Setup
- Java Version: **JDK 23 (Temurin distribution)**
- Build Tool: **Maven**
- Plugins and Tools:
  - `maven-checkstyle-plugin` for static analysis.
  - `jacoco-maven-plugin` (run via CLI) for code coverage.
  - `maven-surefire-plugin` for running tests.

### Job Steps
| Step | Description | Output |
|------|--------------|---------|
| Checkout Repository | Clones the latest version of the repository. | Source files available for build. |
| Set up Java | Installs and configures JDK 23. | Java environment ready for Maven. |
| Run Checkstyle | Executes static analysis during Maven `validate`. | `checkstyle.xml` |
| Upload Checkstyle Report | Uploads XML report as an artifact. | Available in GitHub Actions. |
| Run Tests with JaCoCo | Executes JUnit tests with coverage agent. | `jacoco.exec` |
| Generate JaCoCo Report | Converts JaCoCo results to XML. | `jacoco.xml` |
| Upload JaCoCo Report | Publishes the final coverage report. | Artifact visible in GitHub Actions. |

---

## Testing Overview

### Part 1 – BarnesAndNoble Tests
- Implemented **specification-based** and **structural-based** tests.
- Verified correct total price calculations and behavior for unavailable items.
- Achieved **100% line, branch, and condition coverage**.
- Used **Mockito** to mock dependent classes and verify interactions.

### Part 3 – Amazon Tests
- **Unit Tests:**
  - Mocked `ShoppingCart` and `PriceRule` dependencies to isolate the `Amazon` class.
  - Verified delegation to `ShoppingCart.add()` and rule aggregation logic.
- **Integration Tests:**
  - Used concrete `PriceRule` implementations and a stateful cart.
  - Simulated adding items, calculating totals, and combining multiple rules.
  - Used `@BeforeEach` to reset in-memory data before each test.

Both test types include:
- `@DisplayName("specification-based")` for behavior-driven tests.
- `@DisplayName("structural-based")` for control-flow and branch coverage tests.

---

## Reports and Artifacts

After each workflow run, two XML reports are automatically uploaded as artifacts:

| Report | Description | Location |
|---------|--------------|-----------|
| `checkstyle.xml` | Static analysis results | GitHub Actions → Artifacts |
| `jacoco.xml` | Test coverage results | GitHub Actions → Artifacts |

These artifacts can be downloaded directly from the GitHub Actions summary page.

---

## Workflow Results Summary

| Step | Status | Result |
|------|--------|---------|
| Static Analysis (Checkstyle) | Passed | No critical violations detected. |
| Unit Tests (JUnit 5) | Passed | All test cases executed successfully. |
| Integration Tests | Passed | Multiple component interactions verified. |
| Coverage (JaCoCo) | Passed | Coverage report generated successfully. |
| Artifacts Uploaded | Completed | `checkstyle.xml` and `jacoco.xml` uploaded. |

All GitHub Actions jobs completed successfully, confirmed by the green checkmark on the latest run.

---

## Commit Message

When submitting your work, use the following commit message format:


