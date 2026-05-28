Feature: Login functionality

  @ui @login @TC-UI-01 @smoke
  Scenario: Successful login with valid credentials
    Given I navigate to the login page
    When I enter email "pratikhekad11@gmail.com" and password "Pratik(09)"
    And I click the login button
    Then I should be redirected to the dashboard page

  @ui @login @negative @TC-NEG-01
  Scenario: Login with Invalid Password - Expect Error Message
    Given I navigate to the login page
    When I enter email "invalid-user@example.com" and password "WrongPassword123!"
    And I click the login button
    Then I should see a login error message

  @ui @login @negative @TC-NEG-01
  Scenario: Login with Blank Fields - Should Stay on Login Page
    Given I navigate to the login page
    When I enter email "" and password ""
    And I click the login button
    Then I should remain on the login page
