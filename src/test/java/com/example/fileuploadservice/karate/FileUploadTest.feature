Feature: File Upload Service API Tests

Background:
  * url baseUrl
  * def orderId = 'TEST-ORDER-' + java.util.UUID.randomUUID()

Scenario: Upload single PDF file successfully
  Given path apiPath + '/upload'
  And param orderId = orderId
  And multipart file files = read('classpath:test-files/test.pdf')
  When method post
  Then status 200
  And match response contains 'Files uploaded successfully for order: ' + orderId

Scenario: Upload multiple PDF files successfully
  Given path apiPath + '/upload'
  And param orderId = orderId
  And multipart file files = read('classpath:test-files/test1.pdf')
  And multipart file files = read('classpath:test-files/test2.pdf')
  When method post
  Then status 200
  And match response contains 'Files uploaded successfully for order: ' + orderId

Scenario: Upload non-PDF file should fail
  Given path apiPath + '/upload'
  And param orderId = orderId
  And multipart file files = read('classpath:test-files/test.txt')
  When method post
  Then status 400
  And match response contains 'Only PDF files are allowed'

Scenario: Upload file exceeding size limit should fail
  Given path apiPath + '/upload'
  And param orderId = orderId
  And multipart file files = read('classpath:test-files/large.pdf')
  When method post
  Then status 400
  And match response contains 'File size exceeds 10MB limit'

Scenario: Upload without order ID should fail
  Given path apiPath + '/upload'
  And multipart file files = read('classpath:test-files/test.pdf')
  When method post
  Then status 400
  And match response contains 'Order ID cannot be null or empty'

Scenario: Check if file exists
  Given path apiPath + '/exists'
  And param orderId = orderId
  And param filename = 'test.pdf'
  When method get
  Then status 200
  And match response == true

Scenario: Check if non-existent file exists
  Given path apiPath + '/exists'
  And param orderId = 'NON-EXISTENT-ORDER'
  And param filename = 'nonexistent.pdf'
  When method get
  Then status 200
  And match response == false 