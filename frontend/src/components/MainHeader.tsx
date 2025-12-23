import React, { useEffect, useState } from 'react';
import { Layout, Menu, Button } from 'antd';
import { useNavigate, useLocation } from 'react-router-dom';
import { HomeOutlined, CloudUploadOutlined, LoginOutlined, LogoutOutlined, AuditOutlined } from '@ant-design/icons';

const { Header } = Layout;

const MainHeader: React.FC = () => {
    const navigate = useNavigate();
    const location = useLocation();
    const [isLoggedIn, setIsLoggedIn] = useState(false);

    useEffect(() => {
        // Kiểm tra đơn giản: Có token trong storage = Đã đăng nhập
        const token = localStorage.getItem('accessToken');
        setIsLoggedIn(!!token);
    }, []);

    const handleLogout = () => {
        localStorage.removeItem('accessToken');
        setIsLoggedIn(false);
        navigate('/login');
        message.info('Đã đăng xuất');
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
                    // Chỉ hiện menu Upload nếu đã đăng nhập
                    isLoggedIn ? { label: 'Upload App', key: '/upload', icon: <CloudUploadOutlined />, onClick: () => navigate('/upload') } : null,


                    isLoggedIn ? { label: 'Admin Duyệt bài', key: '/admin', icon: <AuditOutlined />, onClick: () => navigate('/admin') } : null,

                ]}
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
// Cần import message từ antd để dùng ở handleLogout (bạn tự thêm import nhé)
import { message } from 'antd';

export default MainHeader;