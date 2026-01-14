Feature: Bike Rental Search Functionality

  @Positive
  Scenario Outline: Search and validate bikes for a specific city and location
    Given I navigate to the Royal Brothers homepage
    When I select the city "<city>"
    And I enter the booking details
      | Pickup Date | <pickupDate> |
      | Pickup Time | 09:00 AM     |
      | Dropoff Date| <dropoffDate>|
      | Dropoff Time| 09:00 PM     |
    And I click on Search
    Then I verify the date and time filter is visible on results page
    When I apply the location filter "<location>"
    Then I collect all bike models and their availability
    And I validate that all displayed bikes are available at "<location>"

    Examples:
      | city      | pickupDate  | dropoffDate | location           |
      | Bangalore | +1 days     | +2 days     | Indiranagar        |

  @Negative
  Scenario: Verify error message for invalid search criteria
    Given I navigate to the Royal Brothers homepage
    When I select the city "Bangalore"
    And I click on Search without entering dates
    Then I should see a validation message requesting date selection