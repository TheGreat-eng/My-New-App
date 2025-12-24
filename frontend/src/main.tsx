import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
//import './index.css'
import App from './App.tsx'

// Import thư viện
import { AuthProvider } from 'react-oidc-context';

// Cấu hình Keycloak
const oidcConfig = {
  authority: 'http://localhost:8081/realms/enterprise-realm',
  client_id: 'eas-client',
  redirect_uri: 'http://localhost:3000',
  post_logout_redirect_uri: 'http://localhost:3000/login',
  scope: 'openid profile email', // ✅ Thêm scope
  onSigninCallback: () => {
    window.history.replaceState({}, document.title, window.location.pathname);
  }
};

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <AuthProvider {...oidcConfig}>
      <App />
    </AuthProvider>
  </StrictMode>,
)
