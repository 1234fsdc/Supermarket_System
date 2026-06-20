import pytest


class TestHealthEndpoint:

    def test_health_check(self, client):
        response = client.get("/user/ai-customer/health")
        assert response.status_code == 200
        data = response.json()
        assert data["status"] == "ok"
        assert data["service"] == "ai-customer-service"

    def test_health_check_method(self, client):
        response = client.get("/user/ai-customer/health")
        assert response.status_code == 200
        assert response.headers["content-type"].startswith("application/json")
