<template>
  <div class="shop-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>店铺营业状态设置</span>
        </div>
      </template>

      <div class="status-content">
        <div class="current-status">
          <h3>当前营业状态</h3>
          <div class="status-display">
            <el-tag v-if="shopStatus === 1" type="success" size="large">营业中</el-tag>
            <el-tag v-else-if="shopStatus === 0" type="danger" size="large">打烊中</el-tag>
            <el-tag v-else type="info" size="large">未设置</el-tag>
          </div>
        </div>

        <el-divider />

        <div class="status-setting">
          <h3>设置营业状态</h3>
          <div class="status-buttons">
            <el-button 
              type="success" 
              size="large" 
              :disabled="shopStatus === 1"
              @click="handleSetStatus(1)"
            >
              <el-icon><Shop /></el-icon>
              开始营业
            </el-button>
            <el-button 
              type="danger" 
              size="large" 
              :disabled="shopStatus === 0"
              @click="handleSetStatus(0)"
            >
              <el-icon><CircleClose /></el-icon>
              停止营业
            </el-button>
          </div>
        </div>

        <el-divider />

        <div class="status-tips">
          <h3>状态说明</h3>
          <ul>
            <li><el-tag type="success">营业中</el-tag> - 用户可以正常下单购买商品</li>
            <li><el-tag type="danger">打烊中</el-tag> - 用户无法下单，店铺暂停营业</li>
          </ul>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Shop, CircleClose } from '@element-plus/icons-vue'
import { getShopStatus, setShopStatus } from '@/api/shop'

const shopStatus = ref(null)

const fetchStatus = async () => {
  try {
    const res = await getShopStatus()
    shopStatus.value = res.data
  } catch (error) {
    console.error('获取店铺状态失败:', error)
  }
}

const handleSetStatus = async (status) => {
  try {
    await setShopStatus(status)
    shopStatus.value = status
    ElMessage.success(status === 1 ? '店铺已开始营业' : '店铺已停止营业')
  } catch (error) {
    console.error('设置店铺状态失败:', error)
    ElMessage.error('设置失败')
  }
}

onMounted(() => {
  fetchStatus()
})
</script>

<style scoped lang="scss">
.shop-container {
  width: 100%;
  max-width: 800px;
  margin: 0 auto;
}

.card-header {
  font-size: 18px;
  font-weight: bold;
}

.status-content {
  padding: 20px;
}

.current-status {
  text-align: center;
  margin-bottom: 30px;

  h3 {
    margin-bottom: 20px;
    color: #333;
  }

  .status-display {
    .el-tag {
      font-size: 20px;
      padding: 10px 30px;
      height: auto;
    }
  }
}

.status-setting {
  text-align: center;
  margin: 30px 0;

  h3 {
    margin-bottom: 20px;
    color: #333;
  }

  .status-buttons {
    display: flex;
    justify-content: center;
    gap: 30px;

    .el-button {
      padding: 15px 40px;
      font-size: 16px;

      .el-icon {
        margin-right: 8px;
      }
    }
  }
}

.status-tips {
  margin-top: 30px;

  h3 {
    margin-bottom: 15px;
    color: #333;
  }

  ul {
    list-style: none;
    padding: 0;

    li {
      margin: 10px 0;
      color: #666;

      .el-tag {
        margin-right: 10px;
      }
    }
  }
}
</style>
