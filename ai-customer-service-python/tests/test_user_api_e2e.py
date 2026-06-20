import pytest
import httpx
import os

AI_SERVICE_BASE_URL = os.getenv("AI_SERVICE_URL", "http://localhost:8083")


@pytest.fixture
def client():
    return httpx.Client(base_url=AI_SERVICE_BASE_URL, timeout=30.0)


class TestUserAPI:

    def test_user_shop_status_public(self):
        with httpx.Client(base_url="http://localhost:8080", timeout=10.0) as client:
            login_res = client.post("/employee/login", json={"username": "admin", "password": "123456"})
            token = login_res.json()["data"]["token"]
            res = client.get("/shop/status", headers={"token": token})
            data = res.json()
            assert data["code"] == 1

    def test_user_category_requires_auth(self):
        with httpx.Client(base_url="http://localhost:8080", timeout=10.0) as client:
            res = client.get("/user/category/list")
            assert res.status_code == 401

    def test_user_login_and_category_list(self):
        with httpx.Client(base_url="http://localhost:8080", timeout=10.0) as client:
            login_res = client.post("/employee/login", json={"username": "admin", "password": "123456"})
            token = login_res.json()["data"]["token"]
            res = client.get("/category/list", headers={"token": token})
            data = res.json()
            assert data["code"] == 1
            assert len(data["data"]) > 0

    def test_product_list(self):
        with httpx.Client(base_url="http://localhost:8080", timeout=10.0) as client:
            login_res = client.post("/employee/login", json={"username": "admin", "password": "123456"})
            token = login_res.json()["data"]["token"]
            res = client.get("/admin/product/list?categoryId=2", headers={"token": token})
            data = res.json()
            assert data["code"] == 1

    def test_product_search(self):
        with httpx.Client(base_url="http://localhost:8080", timeout=10.0) as client:
            login_res = client.post("/employee/login", json={"username": "admin", "password": "123456"})
            token = login_res.json()["data"]["token"]
            res = client.get("/admin/product/page?page=1&pageSize=10&name=可乐", headers={"token": token})
            data = res.json()
            assert data["code"] == 1

    def test_order_statistics(self):
        with httpx.Client(base_url="http://localhost:8080", timeout=10.0) as client:
            login_res = client.post("/employee/login", json={"username": "admin", "password": "123456"})
            token = login_res.json()["data"]["token"]
            res = client.get("/order/statistics", headers={"token": token})
            data = res.json()
            assert data["code"] == 1

    def test_coupon_template_page(self):
        with httpx.Client(base_url="http://localhost:8080", timeout=10.0) as client:
            login_res = client.post("/employee/login", json={"username": "admin", "password": "123456"})
            token = login_res.json()["data"]["token"]
            res = client.get("/admin/coupon/template/page?page=1&pageSize=10", headers={"token": token})
            data = res.json()
            assert data["code"] == 1

    def test_seckill_activity_in_progress(self):
        with httpx.Client(base_url="http://localhost:8080", timeout=10.0) as client:
            login_res = client.post("/employee/login", json={"username": "admin", "password": "123456"})
            token = login_res.json()["data"]["token"]
            res = client.get("/admin/seckill/activity/inProgress", headers={"token": token})
            data = res.json()
            assert data["code"] == 1

    def test_employee_page(self):
        with httpx.Client(base_url="http://localhost:8080", timeout=10.0) as client:
            login_res = client.post("/employee/login", json={"username": "admin", "password": "123456"})
            token = login_res.json()["data"]["token"]
            res = client.get("/employee/page?page=1&pageSize=10", headers={"token": token})
            data = res.json()
            assert data["code"] == 1
            assert data["data"]["total"] >= 3

    def test_invalid_credentials(self):
        with httpx.Client(base_url="http://localhost:8080", timeout=10.0) as client:
            res = client.post("/employee/login", json={"username": "admin", "password": "wrong"})
            data = res.json()
            assert data["code"] == 0


class TestAICustomerService:

    def test_health_check(self, client):
        try:
            res = client.get("/user/ai-customer/health")
            assert res.status_code == 200
            data = res.json()
            assert data["status"] == "ok"
        except httpx.ConnectError:
            pytest.skip("AI service not running")

    def test_ask_empty(self, client):
        try:
            res = client.get("/user/ai-customer/ask")
            data = res.json()
            assert "answer" in data
        except httpx.ConnectError:
            pytest.skip("AI service not running")

    def test_ask_valid_question(self, client):
        try:
            res = client.get("/user/ai-customer/ask", params={"question": "配送费是多少"})
            data = res.json()
            assert "answer" in data
        except httpx.ConnectError:
            pytest.skip("AI service not running")
