<template>
  <div class="app-container">
    <AppNavigation v-if="showNavigation" />
    <main class="app-main">
      <router-view />
    </main>
  </div>
</template>

<script>
import AppNavigation from './components/AppNavigation.vue'

export default {
  name: 'App',
  components: { AppNavigation },
  computed: {
    showNavigation() {
      return !['Login', 'Register'].includes(this.$route.name)
    }
  }
}
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', 'Oxygen',
    'Ubuntu', 'Cantarell', 'Fira Sans', 'Droid Sans', 'Helvetica Neue',
    'Microsoft YaHei', Arial, sans-serif;
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
  color: var(--gray-800);
  line-height: 1.6;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

.app-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.app-main {
  flex: 1;
  padding: 24px 0 48px;
}
</style>
