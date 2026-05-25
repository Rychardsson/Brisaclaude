<template>
  <div id="app">
    <NavBar v-if="showNavBar" />
    <router-view />
  </div>
</template>

<script>
import { computed } from 'vue';
import { useRoute } from 'vue-router';
import NavBar from '@/components/NavBar.vue';

export default {
  name: 'App',
  components: {
    NavBar
  },
  setup() {
    const route = useRoute();
    
    // Mostrar NavBar apenas em rotas autenticadas (não na tela de login)
    const showNavBar = computed(() => Boolean(route.meta?.requiresAuth));

    return {
      showNavBar
    };
  }
};
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, sans-serif;
  background: #eef3f8;
}

#app {
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  min-height: 100vh;
}
</style>
