import React, { useState } from 'react';
import { Form, Input, Button, Card, message, Typography } from 'antd';
import { UserOutlined, LockOutlined, MailOutlined, SmileOutlined } from '@ant-design/icons';
import { useNavigate, Link } from 'react-router-dom';
import { authApi } from '../api/authApi';

const { Title } = Typography;

const RegisterPage: React.FC = () => {
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();

    const onFinish = async (values: any) => {
        setLoading(true);
        try {
            await authApi.register(values);
            message.success('Đăng ký thành công! Vui lòng đăng nhập.');
            navigate('/login'); // Chuyển ngay sang trang Login
        } catch (error: any) {
            // Hiển thị lỗi từ Backend trả về (nếu có)
            const errorMsg = error.response?.data || 'Đăng ký thất bại!';
            message.error(errorMsg);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh', background: '#f0f2f5' }}>
            <Card style={{ width: 400, boxShadow: '0 4px 12px rgba(0,0,0,0.1)' }}>
                <div style={{ textAlign: 'center', marginBottom: 20 }}>
                    <Title level={3}>Đăng Ký Tài Khoản</Title>
                    <p>Tham gia Enterprise App Store</p>
                </div>

                <Form
                    name="register"
                    onFinish={onFinish}
                    size="large"
                    layout="vertical"
                >
                    <Form.Item
                        name="fullName"
                        rules={[{ required: true, message: 'Nhập họ tên!' }]}
                    >
                        <Input prefix={<SmileOutlined />} placeholder="Họ và tên" />
                    </Form.Item>

                    <Form.Item
                        name="email"
                        rules={[
                            { required: true, message: 'Nhập email!' },
                            { type: 'email', message: 'Email không hợp lệ!' }
                        ]}
                    >
                        <Input prefix={<MailOutlined />} placeholder="Email công ty" />
                    </Form.Item>

                    <Form.Item
                        name="username"
                        rules={[{ required: true, message: 'Nhập username!' }]}
                    >
                        <Input prefix={<UserOutlined />} placeholder="Username" />
                    </Form.Item>

                    <Form.Item
                        name="password"
                        rules={[{ required: true, message: 'Nhập password!' }]}
                    >
                        <Input.Password prefix={<LockOutlined />} placeholder="Password" />
                    </Form.Item>

                    <Form.Item>
                        <Button type="primary" htmlType="submit" block loading={loading}>
                            Đăng ký
                        </Button>
                        <div style={{ marginTop: 10, textAlign: 'center' }}>
                            Đã có tài khoản? <Link to="/login">Đăng nhập ngay</Link>
                        </div>
                    </Form.Item>
                </Form>
            </Card>
        </div>
    );
};

export default RegisterPage;