import pytest
import httpx
import os

AI_SERVICE_BASE_URL = os.getenv("AI_SERVICE_URL", "http://localhost:8083")


@pytest.fixture
def client():
    return httpx.Client(base_url=AI_SERVICE_BASE_URL, timeout=30.0)


@pytest.fixture
def async_client():
    return httpx.AsyncClient(base_url=AI_SERVICE_BASE_URL, timeout=30.0)
