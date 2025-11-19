#!/bin/bash
curl -X POST http://localhost:8080/api/v1/issues \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Broken streetlight on Main St",
    "description": "The streetlight has been out for 3 days",
    "category": "STREETLIGHT",
    "latitude": 32.8968,
    "longitude": 13.1807,
    "address": "123 Main St",
    "reportedBy": "John Doe",
    "reporterEmail": "john@example.com"
  }'

