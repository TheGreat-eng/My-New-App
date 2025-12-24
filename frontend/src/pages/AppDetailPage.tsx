import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Card, List, Button, Typography, Tag, Spin, Empty } from 'antd';
import { DownloadOutlined, ArrowLeftOutlined, HistoryOutlined } from '@ant-design/icons';
import { appApi } from '../api/appApi';
import { type IAppDetail } from '../types';
import { QRCodeSVG } from 'qrcode.react';

const { Title, Paragraph } = Typography;

const AppDetailPage: React.FC = () => {
    const { id } = useParams(); // Lấy ID từ URL
    const navigate = useNavigate();
    const [app, setApp] = useState<IAppDetail | null>(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        if (id) fetchDetail();
    }, [id]);

    const fetchDetail = async () => {
        try {
            const res = await appApi.getAppDetail(Number(id));
            setApp(res.data);
        } catch (error) {
            console.error(error);
        } finally {
            setLoading(false);
        }
    };

    if (loading) return <div style={{ textAlign: 'center', marginTop: 50 }}><Spin size="large" /></div>;
    if (!app) return <Empty description="Không tìm thấy ứng dụng" />;

    return (
        <div style={{ maxWidth: 1000, margin: '20px auto', padding: 20 }}>
            <Button icon={<ArrowLeftOutlined />} onClick={() => navigate(-1)} style={{ marginBottom: 20 }}>
                Quay lại
            </Button>

            {/* Phần thông tin chung */}
            <Card style={{ marginBottom: 20 }}>
                <div style={{ display: 'flex', gap: 20 }}>
                    <div style={{ width: 100, height: 100, background: '#f0f2f5', borderRadius: 16 }}></div>
                    <div style={{ flex: 1 }}>
                        <Title level={2}>{app.name}</Title>
                        <Tag color="blue">{app.packageName}</Tag>
                        <Paragraph style={{ marginTop: 10 }}>{app.description}</Paragraph>
                    </div>
                </div>
            </Card>

            {/* Danh sách lịch sử phiên bản */}
            <Card title={<><HistoryOutlined /> Lịch sử phiên bản</>}>
                <List
                    itemLayout="horizontal"
                    dataSource={app.versions}
                    renderItem={(ver) => (
                        <List.Item
                            extra={
                                <div style={{ textAlign: 'center' }}>
                                    {/* QR Code trỏ tới API Install của Backend */}
                                    <QRCodeSVG
                                        //value={`http://172.20.10.2:8080/api/install/${ver.id}`}
                                        value={`https://eye-partial-mattress-marijuana.trycloudflare.com/api/install/${ver.id}`}
                                        size={100}
                                    />
                                    <div style={{ fontSize: 10, marginTop: 5 }}>Quét để cài</div>
                                </div>
                            }
                            actions={[
                                <Button
                                    type="primary"
                                    icon={<DownloadOutlined />}
                                    onClick={() => window.open(ver.downloadUrl, '_blank')}
                                >
                                    Tải v{ver.latestVersion}
                                </Button>
                            ]}
                        >
                            <List.Item.Meta
                                title={<span style={{ fontWeight: 'bold' }}>Phiên bản {ver.latestVersion}</span>}
                                description={
                                    <div>
                                        <div style={{ fontSize: 12, color: '#888' }}>
                                            Cập nhật: {new Date(ver.updatedAt).toLocaleDateString()}
                                        </div>
                                        <div style={{ marginTop: 5, background: '#f9f9f9', padding: 8, borderRadius: 4 }}>
                                            📝 Release Note: {ver.description || 'Không có ghi chú'}
                                        </div>
                                    </div>
                                }
                            />
                        </List.Item>
                    )}
                />
            </Card>
        </div>
    );
};

export default AppDetailPage;