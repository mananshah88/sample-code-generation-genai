Feature: File Upload API Tests

Background:
    * url baseUrl
    * def testFilePath = 'test-file.txt'
    * def testFileContent = 'This is a test file content'

Scenario: Upload a file successfully
    # Create a test file
    * def FileUtils = Java.type('org.apache.commons.io.FileUtils')
    * FileUtils.writeStringToFile(new java.io.File(testFilePath), testFileContent, 'UTF-8')
    
    # Prepare multipart request
    Given path '/api/v1/files/upload'
    And multipart file file = { read: testFilePath, filename: 'test-file.txt', contentType: 'text/plain' }
    And multipart field orderId = '12345'
    When method POST
    Then status 200
    And match response contains { id: '#notnull', fileName: 'test-file.txt', fileType: 'text/plain', orderId: '12345' }
    
    # Clean up
    * FileUtils.deleteQuietly(new java.io.File(testFilePath))

Scenario: Upload a file with missing orderId
    # Create a test file
    * def FileUtils = Java.type('org.apache.commons.io.FileUtils')
    * FileUtils.writeStringToFile(new java.io.File(testFilePath), testFileContent, 'UTF-8')
    
    # Prepare multipart request
    Given path '/api/v1/files/upload'
    And multipart file file = { read: testFilePath, filename: 'test-file.txt', contentType: 'text/plain' }
    When method POST
    Then status 400
    
    # Clean up
    * FileUtils.deleteQuietly(new java.io.File(testFilePath))

Scenario: Upload a file with empty file
    Given path '/api/v1/files/upload'
    And multipart field orderId = '12345'
    When method POST
    Then status 400

Scenario: Upload a file with invalid file type
    # Create a test file
    * def FileUtils = Java.type('org.apache.commons.io.FileUtils')
    * FileUtils.writeStringToFile(new java.io.File(testFilePath), testFileContent, 'UTF-8')
    
    # Prepare multipart request
    Given path '/api/v1/files/upload'
    And multipart file file = { read: testFilePath, filename: 'test-file.exe', contentType: 'application/x-msdownload' }
    And multipart field orderId = '12345'
    When method POST
    Then status 400
    
    # Clean up
    * FileUtils.deleteQuietly(new java.io.File(testFilePath))
