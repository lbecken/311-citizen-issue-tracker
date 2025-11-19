#!/bin/bash
# Test geocoding endpoint
#curl "http://localhost:8080/api/v1/location/reverse?lat=40.7128&lon=-74.0060"

# Test image upload (after creating an issue)
#curl -X POST "http://localhost:8080/api/v1/issues/{ISSUE_ID}/images" \
#  -F "file=@/path/to/test-image.jpg"

# Test image download
# In browser OR using curl
#curl "http://localhost:8080/api/v1/images/123e7db3-aa36-4f4d-b401-2952d58703dd/file"
