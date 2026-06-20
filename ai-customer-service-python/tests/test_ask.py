import pytest


class TestAskEndpoint:

    def test_ask_without_question(self, client):
        response = client.get("/user/ai-customer/ask")
        assert response.status_code == 200
        data = response.json()
        assert "answer" in data
        assert data["answer"] == "请输入问题"

    def test_ask_with_empty_question(self, client):
        response = client.get("/user/ai-customer/ask", params={"question": ""})
        assert response.status_code == 200
        data = response.json()
        assert data["answer"] == "请输入问题"

    def test_ask_with_valid_question(self, client):
        response = client.get("/user/ai-customer/ask", params={"question": "配送费是多少"})
        assert response.status_code == 200
        data = response.json()
        assert "answer" in data
        assert isinstance(data.get("products", []), list)

    def test_ask_with_session(self, client):
        response = client.get(
            "/user/ai-customer/ask",
            params={"question": "你好", "session_id": "test-session-001"}
        )
        assert response.status_code == 200
        assert "answer" in response.json()

    def test_ask_response_structure(self, client):
        response = client.get("/user/ai-customer/ask", params={"question": "有什么商品"})
        assert response.status_code == 200
        data = response.json()
        assert "answer" in data
        assert isinstance(data["answer"], str)
        assert len(data["answer"]) > 0
