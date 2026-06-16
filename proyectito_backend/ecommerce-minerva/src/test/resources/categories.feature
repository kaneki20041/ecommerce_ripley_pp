Feature: Test API de Categorías Públicas

  Background:
    * url baseUrl
    * configure logPrettyRequest = true
    * configure logPrettyResponse = true

  Scenario: Obtener el árbol de categorías públicas
    Given path '/api/public/categories'
    When method GET
    Then status 200
    And match response.result == true
    And match response.message == "Árbol de categorías recuperado con éxito"
    And match response.data == '#array'
