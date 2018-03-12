# Testing

## 1. Overview

- Unit test
    + assertion
    + test suits
    + test SetUp
- Unit test frameworks
    + JUnit
    + testNG
- Mock Frameworks
    + EasyMock
    + Mockito
    + jMock
- Development
    - Continuous Integration
    - Test Driven Development
    - Behavior Driven Development
- Tester
    + Boundary Values
    + Equivalence Classes
    + Block-box testing
    + White-box testing
    + Test Case
    + Bug Report
    + Definitions
        * Issue
        * Bug
        * Defect
        * Fault
    + Testing
        * System
        * Integration
        * UserAcceptance
        * Smoke
        * Regression
    + Automation
        * when to automate
        * pros and cons


## 2. JUnit

- Unit Test: testing of single entity (class or method)
- Manual Test: without tool support
- Automated Test: tool support, test cases
    + Fast, less human work, reliable, programmable
- JUnit
    + java org.junit.runner.JUnitCore
    + regression testing framework
    + open source
    + annotation/assertion/testrunner
- Best Practice
    + known input -> expected output
    + two cases for each requirement: positive + negative
    + Not use println(): need manual scanning
- Notes
    + Only report the first failure
    + JUnit 4 compatible with assert keyword
    + JUnit 3.7 replace with assertTrue()


Core Features
- Fixture
    + fixed state of a set of objects
    + setUp() & tearDown()
- Test Suite
    + bundle a few unit test case together
    + annotation: @RunWith and @Suite
- Test Runner
    + executor
- JUnit Classes
    + Assert: set of assertion methods
    + TestCase
    + TestResult: collecting parameter pattern
    + TestSuite: composite of tests
- Annotations
    + @Test: the public void method to which it is attached can be run as a test case.
    + @Before: run before each test method
    + @After: release external resource in before
    + @BeforeClass: run once before all method
    + @AfterClass: clean up activities
    + @Ignore: cannot fix error but keep
    + @Test(expected): trace desire exception
- Parameterized test
    + run again with different value
- Test protected method
    + put test class to the same package
- use main() method for testing
    + advantage: whitebox test
    + test internals like private
    + but usual test are from user perspective
- Garbage
    + testrunner tearDown() method
- Mock object
    + simulate behavior of complex real objects
    + create instance
    + set state and expectation
