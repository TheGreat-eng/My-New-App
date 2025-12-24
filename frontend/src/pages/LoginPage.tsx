import React, { useEffect } from 'react';
import { Button, Card, Typography, message } from 'antd';
import { LoginOutlined } from '@ant-design/icons';
import { useAuth } from 'react-oidc-context';
import { useNavigate, useSearchParams } from 'react-router-dom';

const { Title } = Typography;

const LoginPage: React.FC = () => {
    const auth = useAuth();
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();

    useEffect(() => {
        // ✅ Kiểm tra nếu có logout callback (có state hoặc error từ Keycloak)
        //const logoutState = searchParams.get('state');
        const hasLogoutParams = searchParams.has('state') || searchParams.has('iss');

        if (hasLogoutParams && !auth.isAuthenticated) {
            // ✅ Đã logout thành công, clean URL
            message.success('Đăng xuất thành công!');
            window.history.replaceState({}, document.title, '/login');
            return;
        }

        // ✅ Chỉ redirect một lần khi đã authenticated
        if (auth.isAuthenticated && auth.user?.access_token) {
            localStorage.setItem('accessToken', auth.user.access_token);
            navigate('/', { replace: true }); // ✅ Dùng replace để không tạo history
        }
    }, [auth.isAuthenticated, auth.user, navigate, searchParams]);

    // ✅ Nếu đang loading thì hiện spinner
    if (auth.isLoading) {
        return (
            <div style={{
                display: 'flex',
                justifyContent: 'center',
                alignItems: 'center',
                height: '100vh'
            }}>
                Đang xử lý...
            </div>
        );
    }

    return (
        <div style={{
            display: 'flex',
            justifyContent: 'center',
            alignItems: 'center',
            height: '100vh',
            background: '#f0f2f5'
        }}>
            <Card style={{ width: 400, textAlign: 'center' }}>
                <Title level={3}>Enterprise App Store</Title>
                <p>Hệ thống đăng nhập tập trung (SSO)</p>

                <div style={{ marginTop: 30 }}>
                    <Button
                        type="primary"
                        size="large"
                        icon={<LoginOutlined />}
                        onClick={() => auth.signinRedirect()}
                        block
                        loading={auth.isLoading}
                    >
                        Đăng nhập bằng Keycloak
                    </Button>
                </div>
            </Card>
        </div>
    );
};

export default LoginPage;