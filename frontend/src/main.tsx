import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
//import './index.css'
import App from './App.tsx'

// Import thư viện
import { AuthProvider } from 'react-oidc-context';

// Cấu hình Keycloak
const oidcConfig = {
  authority: 'http://localhost:8081/realms/enterprise-realm', // Địa chỉ Keycloak
  client_id: 'eas-client', // Tên Client ID bạn tạo lúc nãy
  redirect_uri: 'http://localhost:3000', // Đăng nhập xong quay về đâu?
  onSigninCallback: () => {
    // Khi đăng nhập xong, xóa bớt mấy cái code loằng ngoằng trên URL cho đẹp
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
