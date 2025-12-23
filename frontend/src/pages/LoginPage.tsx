import React, { useState } from 'react';
import { Form, Input, Button, Card, message, Typography } from 'antd';
import { UserOutlined, LockOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { authApi } from '../api/authApi';

const { Title } = Typography;

const LoginPage: React.FC = () => {
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();

    const onFinish = async (values: any) => {
        setLoading(true);
        try {
            // 1. Gọi API Login
            const res = await authApi.login(values);

            // 2. Lưu token vào LocalStorage
            localStorage.setItem('accessToken', res.data.accessToken);

            message.success('Đăng nhập thành công!');

            // 3. Chuyển hướng về trang chủ (hoặc trang Upload)
            navigate('/');
            // Refresh nhẹ một cái để cập nhật state header (nếu có)
            window.location.reload();

        } catch (error) {
            message.error('Sai tài khoản hoặc mật khẩu!');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh', background: '#f0f2f5' }}>
            <Card style={{ width: 400, boxShadow: '0 4px 12px rgba(0,0,0,0.1)' }}>
                <div style={{ textAlign: 'center', marginBottom: 30 }}>
                    <Title level={3}>Enterprise App Store</Title>
                    <p>Đăng nhập hệ thống nội bộ</p>
                </div>

                <Form
                    name="login"
                    initialValues={{ remember: true }}
                    onFinish={onFinish}
                    size="large"
                >
                    <Form.Item
                        name="username"
                        rules={[{ required: true, message: 'Vui lòng nhập Username!' }]}
                    >
                        <Input prefix={<UserOutlined />} placeholder="Username (admin_sys)" />
                    </Form.Item>

                    <Form.Item
                        name="password"
                        rules={[{ required: true, message: 'Vui lòng nhập Password!' }]}
                    >
                        <Input.Password prefix={<LockOutlined />} placeholder="Password" />
                    </Form.Item>

                    <Form.Item>
                        <Button type="primary" htmlType="submit" block loading={loading}>
                            Đăng nhập
                        </Button>
                    </Form.Item>
                </Form>
            </Card>
        </div>
    );
};

export default LoginPage;