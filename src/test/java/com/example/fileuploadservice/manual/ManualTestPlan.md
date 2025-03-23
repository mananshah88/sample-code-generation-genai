# File Upload Service - Manual Test Plan

## Overview
This document outlines the manual test cases for the File Upload Service. The service allows users to upload PDF files with associated order IDs, with validation for file type, size, and duplicates.

## Test Environment Setup
1. Ensure the application is running locally
2. Configure test S3 bucket credentials
3. Set up test database
4. Prepare test files of various sizes and types

## Test Cases

### 1. Basic File Upload Functionality
#### 1.1 Single PDF Upload
- **Prerequisites**: Valid PDF file (< 10MB)
- **Steps**:
  1. Send POST request to `/api/files/upload` with:
     - orderId: "TEST-ORDER-001"
     - files: Single PDF file
  2. Verify response status is 200
  3. Check S3 bucket for uploaded file
  4. Verify database entry
- **Expected Result**: File successfully uploaded and accessible

#### 1.2 Multiple PDF Upload
- **Prerequisites**: Multiple valid PDF files
- **Steps**:
  1. Send POST request with multiple files
  2. Verify all files are uploaded
  3. Check database entries
- **Expected Result**: All files uploaded successfully

### 2. Input Validation Tests
#### 2.1 Non-PDF File Upload
- **Test Cases**:
  - [ ] Upload .txt file
  - [ ] Upload .doc file
  - [ ] Upload .jpg file
- **Expected Result**: 400 Bad Request with appropriate error message

#### 2.2 File Size Validation
- **Test Cases**:
  - [ ] Upload 11MB PDF file
  - [ ] Upload 9.9MB PDF file
  - [ ] Upload 1MB PDF file
- **Expected Result**: Files > 10MB rejected with appropriate error

#### 2.3 Empty File Upload
- **Steps**:
  1. Create empty PDF file
  2. Attempt upload
- **Expected Result**: 400 Bad Request with "File cannot be empty" message

### 3. Order ID Validation
#### 3.1 Missing Order ID
- **Steps**:
  1. Send request without orderId parameter
- **Expected Result**: 400 Bad Request with "Order ID cannot be null or empty"

#### 3.2 Invalid Order ID
- **Test Cases**:
  - [ ] Empty string
  - [ ] Only spaces
  - [ ] Special characters
  - [ ] Very long string
- **Expected Result**: Appropriate error messages

### 4. Duplicate File Handling
#### 4.1 Same File Upload
- **Steps**:
  1. Upload file for order ID
  2. Attempt to upload same file again
- **Expected Result**: 400 Bad Request with duplicate file message

#### 4.2 Case Sensitivity
- **Steps**:
  1. Upload "Test.pdf"
  2. Attempt to upload "test.pdf"
- **Expected Result**: Duplicate detection should be case-sensitive

### 5. API Response Validation
#### 5.1 Success Response
- **Expected Format**:
  ```json
  {
    "status": 200,
    "message": "Files uploaded successfully for order: {orderId}"
  }
  ```

#### 5.2 Error Response
- **Expected Format**:
  ```json
  {
    "status": 400,
    "message": "Error message"
  }
  ```

### 6. Rate Limiting Tests
#### 6.1 Request Rate
- **Steps**:
  1. Send multiple requests within short time
  2. Monitor response headers
- **Expected Result**: Rate limit headers present, requests limited

### 7. Circuit Breaker Tests
#### 7.1 S3 Service Failure
- **Steps**:
  1. Simulate S3 service down
  2. Attempt file uploads
  3. Monitor circuit breaker state
- **Expected Result**: Circuit breaker opens after failures

### 8. Concurrent Upload Tests
#### 8.1 Multiple Simultaneous Uploads
- **Steps**:
  1. Send multiple upload requests simultaneously
  2. Monitor system behavior
- **Expected Result**: All uploads handled correctly

### 9. File Content Validation
#### 9.1 Corrupted PDF
- **Steps**:
  1. Create corrupted PDF file
  2. Attempt upload
- **Expected Result**: Appropriate error handling

### 10. Security Tests
#### 10.1 Malicious File Upload
- **Test Cases**:
  - [ ] PDF with embedded scripts
  - [ ] PDF with malicious content
- **Expected Result**: Files rejected with security message

### 11. Integration Tests
#### 11.1 S3 Integration
- **Steps**:
  1. Upload file
  2. Verify S3 bucket contents
  3. Check file permissions
- **Expected Result**: Files properly stored in S3

### 12. Performance Tests
#### 12.1 Large File Upload
- **Steps**:
  1. Upload file near size limit
  2. Monitor memory usage
  3. Check response time
- **Expected Result**: System handles large files efficiently

### 13. Error Recovery
#### 13.1 Network Interruption
- **Steps**:
  1. Start file upload
  2. Simulate network failure
  3. Restore network
- **Expected Result**: Appropriate error handling and recovery

### 14. Documentation Verification
#### 14.1 API Documentation
- **Check Points**:
  - [ ] Endpoint descriptions
  - [ ] Request/response formats
  - [ ] Error codes
  - [ ] Example requests

## Test Execution Template
For each test case, document:
1. Test ID
2. Test Description
3. Prerequisites
4. Test Steps
5. Expected Results
6. Actual Results
7. Pass/Fail Status
8. Issues Found
9. Screenshots/Logs

## Test Results Summary
- Total Test Cases: XX
- Passed: XX
- Failed: XX
- Blocked: XX

## Notes
- Keep test data separate from production
- Document any environment-specific issues
- Update test cases as new features are added
- Maintain test execution logs 