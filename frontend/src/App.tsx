import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import UploadPage from './pages/UploadPage';
import 'antd/dist/reset.css'; // Reset CSS của Antd (quan trọng)
import HomePage from './pages/HomePage';
import { Layout } from 'antd';
import MainHeader from './components/MainHeader';
import type { JSX } from 'react';
import LoginPage from './pages/LoginPage'
import AdminDashboard from './pages/AdminDashboard';
import AppDetailPage from './pages/AppDetailPage';
import RegisterPage from './pages/RegisterPage';
import { useAuth } from 'react-oidc-context';


// Component Bảo vệ: Nếu chưa có token thì đá về Login
const ProtectedRoute = ({ children }: { children: JSX.Element }) => {
  const token = localStorage.getItem('accessToken');
  if (!token) {
    return <Navigate to="/login" replace />;
  }
  return children;
};


function App() {



  const auth = useAuth();

  // Thêm đoạn này để hiện Loading khi đang xử lý Code từ Keycloak
  if (auth.isLoading) {
    return <div style={{ textAlign: 'center', marginTop: 50 }}>Đang xử lý đăng nhập...</div>;
  }

  // Nếu bị lỗi
  if (auth.error) {
    return <div>Lỗi đăng nhập: {auth.error.message}</div>;
  }

  return (
    <Router>
      <Layout style={{ minHeight: '100vh' }}>
        <MainHeader /> {/* Header luôn hiển thị */}

        <Layout.Content>
          <Routes>
            <Route path="/" element={<HomePage />} />
            <Route path="/login" element={<LoginPage />} />

            {/* Route này cần bảo mật */}
            <Route
              path="/upload"
              element={
                <ProtectedRoute>
                  <UploadPage />
                </ProtectedRoute>
              }
            />




            <Route path="/admin" element={
              <ProtectedRoute>
                <AdminDashboard />
              </ProtectedRoute>
            } />

            <Route path="/apps/:id" element={<AppDetailPage />} />


            <Route path="/register" element={<RegisterPage />} />


          </Routes>
        </Layout.Content>
      </Layout>
    </Router>
  );
}

export default App;