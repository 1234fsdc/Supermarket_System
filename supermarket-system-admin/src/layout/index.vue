<template>
  <div class="admin-container">
    <div class="sidebar">
      <div class="logo">
        <el-icon :size="28" color="#f59e0b"><Food /></el-icon>
        <span class="logo-text">凡栋超市</span>
      </div>
      <div class="menu-container">
        <div 
          v-for="item in menuItems" 
          :key="item.path"
          class="menu-item"
          :class="{ active: isActive(item) }"
          @click="handleMenuClick(item)"
        >
          <el-icon class="menu-icon"><component :is="item.icon" /></el-icon>
          <span class="menu-text">{{ item.title }}</span>
        </div>
      </div>
    </div>

    <div class="main-wrapper">
      <div class="header">
        <div class="header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item>{{ currentTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-button type="danger" size="small" @click="handleLogout">退出登录</el-button>
        </div>
      </div>

      <div class="main-content">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  DataBoard,
  DataLine,
  List,
  Goods,
  Menu,
  UserFilled,
  Food,
  Shop
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()

const menuItems = ref([
  {
    title: '工作台',
    path: '/dashboard',
    icon: 'DataBoard'
  },
  {
    title: '数据统计',
    path: '/statistics',
    icon: 'DataLine'
  },
  {
    title: '订单管理',
    path: '/order',
    icon: 'List'
  },
  {
    title: '商品管理',
    path: '/product',
    icon: 'Goods'
  },
  {
    title: '分类管理',
    path: '/category',
    icon: 'Menu'
  },
  {
    title: '员工管理',
    path: '/employee',
    icon: 'UserFilled'
  },
  {
    title: '店铺状态',
    path: '/shop',
    icon: 'Shop'
  }
])

const currentPath = ref(route.path)

watch(
  () => route.path,
  (newPath) => {
    currentPath.value = newPath
  }
)

const isActive = (item) => {
  return currentPath.value === item.path || currentPath.value.startsWith(item.path + '/')
}

const currentTitle = computed(() => {
  const activeItem = menuItems.value.find(item => isActive(item))
  return activeItem ? activeItem.title : ''
})

const handleMenuClick = (item) => {
  router.push(item.path)
}

const handleLogout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')
  router.push('/login')
}


</script>

<style scoped lang="scss">
.admin-container {
  display: flex;
  height: 100vh;
  width: 100%;
}

.sidebar {
  width: 240px;
  background: #1e1e2d;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #161625;
  gap: 10px;
}

.logo-text {
  color: #fff;
  font-size: 18px;
  font-weight: 600;
  letter-spacing: 2px;
}

.menu-container {
  flex: 1;
  padding: 10px 0;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 14px 20px;
  color: #9ca3af;
  cursor: pointer;
  transition: all 0.2s;
  gap: 12px;
  position: relative;

  &:hover {
    color: #fff;
    background: rgba(255, 255, 255, 0.05);
  }

  &.active {
    color: #fff;
    background: #f59e0b;
    
    .menu-icon {
      color: #fff;
    }
  }
}

.menu-icon {
  font-size: 18px;
  transition: color 0.2s;
}

.menu-text {
  font-size: 14px;
}

.main-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.header {
  height: 60px;
  background: #f59e0b;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.header-left {
  :deep(.el-breadcrumb) {
    .el-breadcrumb__item {
      .el-breadcrumb__inner {
        color: #fff;
        
        &.is-link:hover {
          color: #fff;
          opacity: 0.8;
        }
      }

      &:last-child {
        .el-breadcrumb__inner {
          color: #fff;
          font-weight: 500;
        }
      }
    }

    .el-breadcrumb__separator {
      color: rgba(255, 255, 255, 0.6);
    }
  }
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: #fff;
  font-size: 14px;
  padding: 5px;
  border-radius: 4px;
  transition: background 0.2s;

  &:hover {
    background: rgba(255, 255, 255, 0.2);
  }
}

.main-content {
  flex: 1;
  background: #f0f2f5;
  padding: 20px;
  overflow-y: auto;
}
</style>
