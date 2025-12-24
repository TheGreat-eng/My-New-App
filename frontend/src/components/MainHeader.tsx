import React, { useEffect, useState } from 'react';
import { Layout, Menu, Button } from 'antd';
import { useNavigate, useLocation } from 'react-router-dom';
import { HomeOutlined, CloudUploadOutlined, LoginOutlined, LogoutOutlined, AuditOutlined } from '@ant-design/icons';
import { useAuth } from 'react-oidc-context';

const { Header } = Layout;

const MainHeader: React.FC = () => {
    const navigate = useNavigate();
    const location = useLocation();
    const auth = useAuth(); // ✅ Di chuyển vào trong component
    const [isLoggedIn, setIsLoggedIn] = useState(false);

    useEffect(() => {
        // ✅ Kiểm tra cả auth.isAuthenticated và localStorage
        const token = localStorage.getItem('accessToken');
        const authenticated = auth.isAuthenticated || !!token;

        // ✅ Chỉ update khi giá trị thay đổi
        if (isLoggedIn !== authenticated) {
            setIsLoggedIn(authenticated);
        }
    }, [auth.isAuthenticated]); // ✅ Thêm dependency

    const handleLogout = () => {
        localStorage.removeItem('accessToken');
        setIsLoggedIn(false); // ✅ Update state ngay lập tức
        auth.signoutRedirect();
    };

    return (
        <Header style={{ display: 'flex', alignItems: 'center', background: '#fff', boxShadow: '0 2px 8px #f0f1f2' }}>
            <div
                style={{ fontWeight: 'bold', fontSize: 18, marginRight: 40, cursor: 'pointer' }}
                onClick={() => navigate('/')}
            >
                🏢 Enterprise Store
            </div>

            <Menu
                mode="horizontal"
                selectedKeys={[location.pathname]}
                style={{ flex: 1, borderBottom: 'none' }}
                items={[
                    { label: 'Trang chủ', key: '/', icon: <HomeOutlined />, onClick: () => navigate('/') },
                    isLoggedIn ? { label: 'Upload App', key: '/upload', icon: <CloudUploadOutlined />, onClick: () => navigate('/upload') } : null,
                    isLoggedIn ? { label: 'Admin Duyệt bài', key: '/admin', icon: <AuditOutlined />, onClick: () => navigate('/admin') } : null,
                ].filter(Boolean)} // ✅ Lọc null values
            />

            <div>
                {isLoggedIn ? (
                    <Button type="text" danger icon={<LogoutOutlined />} onClick={handleLogout}>
                        Đăng xuất
                    </Button>
                ) : (
                    <Button type="primary" icon={<LoginOutlined />} onClick={() => navigate('/login')}>
                        Đăng nhập
                    </Button>
                )}
            </div>
        </Header>
    );
};

export default MainHeader;