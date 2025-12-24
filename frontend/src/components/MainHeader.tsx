import React, { useEffect, useState } from 'react';
import { Layout, Menu, Button } from 'antd';
import { useNavigate, useLocation } from 'react-router-dom';
import { HomeOutlined, CloudUploadOutlined, LoginOutlined, LogoutOutlined, AuditOutlined } from '@ant-design/icons';
import { useAuth } from 'react-oidc-context';

const { Header } = Layout;

const MainHeader: React.FC = () => {
    const navigate = useNavigate();
    const location = useLocation();
    const auth = useAuth();
    const [isLoggedIn, setIsLoggedIn] = useState(() => {
        // ✅ Khởi tạo state từ localStorage ngay từ đầu
        return !!localStorage.getItem('accessToken') || auth.isAuthenticated;
    });

    useEffect(() => {
        // ✅ Đồng bộ state khi auth thay đổi
        const token = localStorage.getItem('accessToken');
        const authenticated = auth.isAuthenticated || !!token;
        
        setIsLoggedIn(authenticated);
    }, [auth.isAuthenticated]);

    const handleLogout = async () => {
        try {
            // ✅ Xóa token trước
            localStorage.removeItem('accessToken');
            setIsLoggedIn(false);
            
            // ✅ Logout từ Keycloak và redirect về trang login
            await auth.signoutRedirect({
                post_logout_redirect_uri: 'http://localhost:3000/login'
            });
        } catch (error) {
            console.error('Lỗi khi đăng xuất:', error);
            // ✅ Nếu có lỗi thì vẫn redirect về login
            window.location.href = '/login';
        }
    };

    // ✅ Tạo menu items bên ngoài JSX để tránh re-create mỗi lần render
    const menuItems = React.useMemo(() => [
        { 
            label: 'Trang chủ', 
            key: '/', 
            icon: <HomeOutlined />, 
            onClick: () => navigate('/') 
        },
        ...(isLoggedIn ? [
            { 
                label: 'Upload App', 
                key: '/upload', 
                icon: <CloudUploadOutlined />, 
                onClick: () => navigate('/upload') 
            },
            { 
                label: 'Admin Duyệt bài', 
                key: '/admin', 
                icon: <AuditOutlined />, 
                onClick: () => navigate('/admin') 
            }
        ] : [])
    ], [isLoggedIn, navigate]);

    return (
        <Header style={{ 
            display: 'flex', 
            alignItems: 'center', 
            background: '#fff', 
            boxShadow: '0 2px 8px #f0f1f2',
            position: 'sticky',
            top: 0,
            zIndex: 1
        }}>
            <div
                style={{ 
                    fontWeight: 'bold', 
                    fontSize: 18, 
                    marginRight: 40, 
                    cursor: 'pointer' 
                }}
                onClick={() => navigate('/')}
            >
                🏢 Enterprise Store
            </div>

            <Menu
                mode="horizontal"
                selectedKeys={[location.pathname]}
                style={{ flex: 1, borderBottom: 'none' }}
                items={menuItems}
            />

            <div>
                {isLoggedIn ? (
                    <Button 
                        type="text" 
                        danger 
                        icon={<LogoutOutlined />} 
                        onClick={handleLogout}
                    >
                        Đăng xuất
                    </Button>
                ) : (
                    <Button 
                        type="primary" 
                        icon={<LoginOutlined />} 
                        onClick={() => navigate('/login')}
                    >
                        Đăng nhập
                    </Button>
                )}
            </div>
        </Header>
    );
};

export default MainHeader;