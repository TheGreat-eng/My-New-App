import React, { useEffect } from 'react';
import { Button, Card, Typography } from 'antd';
import { LoginOutlined } from '@ant-design/icons';
import { useAuth } from 'react-oidc-context'; // Hook lấy thông tin Auth
import { useNavigate } from 'react-router-dom';

const { Title } = Typography;

const LoginPage: React.FC = () => {
    const auth = useAuth();
    const navigate = useNavigate();

    // Nếu đã đăng nhập rồi thì đá về trang chủ
    useEffect(() => {
        // Ưu tiên check auth của thư viện trước
        if (auth.isAuthenticated) {
            localStorage.setItem('accessToken', auth.user?.access_token || '');
            navigate('/');
        }
        // Fallback: Nếu thư viện chưa kịp cập nhật nhưng LocalStorage đã có hàng
        // (Cẩn thận đoạn này: authToken trong ảnh của bạn tên là gì? access_token hay authToken?)
        // Trong ảnh bạn gửi mình thấy key là 'access_token'.
        else if (localStorage.getItem('access_token')) {
            navigate('/');
        }
    }, [auth.isAuthenticated, navigate]);

    return (
        <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh', background: '#f0f2f5' }}>
            <Card style={{ width: 400, textAlign: 'center' }}>
                <Title level={3}>Enterprise App Store</Title>
                <p>Hệ thống đăng nhập tập trung (SSO)</p>

                <div style={{ marginTop: 30 }}>
                    {/* Bấm nút này sẽ chuyển sang Keycloak */}
                    <Button
                        type="primary"
                        size="large"
                        icon={<LoginOutlined />}
                        onClick={() => auth.signinRedirect()}
                        block
                    >
                        Đăng nhập bằng Keycloak
                    </Button>
                </div>
            </Card>
        </div>
    );
};

export default LoginPage;