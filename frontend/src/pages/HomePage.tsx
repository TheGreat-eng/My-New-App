import React, { useEffect, useState } from 'react';
import { Card, Button, Row, Col, Typography, Tag, Spin, Empty } from 'antd';
import { DownloadOutlined, AndroidOutlined } from '@ant-design/icons';
import { appApi } from '../api/appApi';
import { type IAppDTO } from '../types';

const { Meta } = Card;
const { Title } = Typography;

const HomePage: React.FC = () => {
    const [apps, setApps] = useState<IAppDTO[]>([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetchApps();
    }, []);

    const fetchApps = async () => {
        try {
            const res = await appApi.getAllApps();
            setApps(res.data);
        } catch (error) {
            console.error(error);
        } finally {
            setLoading(false);
        }
    };

    const handleDownload = (url: string) => {
        // Mở link presigned url trong tab mới để tải
        window.open(url, '_blank');
    };

    return (
        <div style={{ padding: '30px', maxWidth: 1200, margin: '0 auto' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 20 }}>
                <Title level={2}>Kho Ứng dụng</Title>
                <Button href="/upload" type="dashed">Đăng tải App mới</Button>
            </div>

            {loading ? (
                <div style={{ textAlign: 'center', marginTop: 50 }}><Spin size="large" /></div>
            ) : apps.length === 0 ? (
                <Empty description="Chưa có ứng dụng nào" />
            ) : (
                <Row gutter={[24, 24]}>
                    {apps.map((app) => (
                        <Col xs={24} sm={12} md={8} lg={6} key={app.id}>
                            <Card
                                hoverable
                                actions={[
                                    <Button
                                        type="primary"
                                        icon={<DownloadOutlined />}
                                        onClick={() => handleDownload(app.downloadUrl)}
                                    >
                                        Tải về v{app.latestVersion}
                                    </Button>
                                ]}
                            >
                                <Meta
                                    avatar={
                                        <div style={{
                                            width: 50, height: 50, background: '#f0f2f5',
                                            borderRadius: 8, display: 'flex', alignItems: 'center', justifyContent: 'center'
                                        }}>
                                            {/* Tạm thời dùng icon mặc định, sau này sẽ load ảnh thật */}
                                            <AndroidOutlined style={{ fontSize: 24, color: '#52c41a' }} />
                                        </div>
                                    }
                                    title={app.name}
                                    description={
                                        <div>
                                            <div style={{ height: 40, overflow: 'hidden', textOverflow: 'ellipsis', marginBottom: 10 }}>
                                                {app.description}
                                            </div>
                                            <Tag color="blue">{app.latestVersion}</Tag>
                                        </div>
                                    }
                                />
                            </Card>
                        </Col>
                    ))}
                </Row>
            )}
        </div>
    );
};

export default HomePage;