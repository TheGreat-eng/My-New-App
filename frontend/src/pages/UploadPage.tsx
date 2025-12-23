import React, { useState } from 'react';
import { Form, Input, Button, Upload, message, Card, Typography } from 'antd';
import { UploadOutlined, InboxOutlined } from '@ant-design/icons';
import { appApi } from '../api/appApi';

const { Title } = Typography;
const { Dragger } = Upload;

const UploadPage: React.FC = () => {
    const [loading, setLoading] = useState(false);
    const [form] = Form.useForm();

    // Hàm xử lý khi người dùng nhấn nút Submit
    const onFinish = async (values: any) => {
        // 1. Lấy file từ object của Antd Upload
        const fileObj = values.file?.[0]?.originFileObj;

        if (!fileObj) {
            message.error('Vui lòng chọn file cài đặt!');
            return;
        }

        // 2. Đóng gói dữ liệu vào FormData
        const formData = new FormData();
        formData.append('name', values.name);
        formData.append('packageName', values.packageName);
        formData.append('description', values.description);
        formData.append('version', values.version);
        formData.append('releaseNote', values.releaseNote);
        formData.append('file', fileObj); // File binary

        setLoading(true);
        try {
            // 3. Gọi API
            await appApi.uploadApp(formData);
            message.success('Upload ứng dụng thành công!');
            form.resetFields(); // Xóa trắng form
        } catch (error) {
            console.error(error);
            message.error('Có lỗi xảy ra khi upload.');
        } finally {
            setLoading(false);
        }
    };

    // Cấu hình chuẩn hóa dữ liệu file cho Antd Form
    const normFile = (e: any) => {
        if (Array.isArray(e)) {
            return e;
        }
        return e?.fileList;
    };

    return (
        <div style={{ maxWidth: 800, margin: '40px auto', padding: 20 }}>
            <Card>
                <Title level={2} style={{ textAlign: 'center', marginBottom: 30 }}>
                    Upload Ứng dụng Nội bộ
                </Title>

                <Form
                    form={form}
                    layout="vertical"
                    onFinish={onFinish}
                    initialValues={{ version: '1.0.0' }}
                >
                    <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 20 }}>
                        <Form.Item
                            label="Tên ứng dụng"
                            name="name"
                            rules={[{ required: true, message: 'Nhập tên App' }]}
                        >
                            <Input placeholder="Vd: HR Mobile Portal" />
                        </Form.Item>

                        <Form.Item
                            label="Package Name (ID)"
                            name="packageName"
                            rules={[{ required: true, message: 'Nhập Package Name' }]}
                        >
                            <Input placeholder="Vd: com.company.hr" />
                        </Form.Item>
                    </div>

                    <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 20 }}>
                        <Form.Item
                            label="Phiên bản (Version)"
                            name="version"
                            rules={[{ required: true, message: 'Nhập version' }]}
                        >
                            <Input placeholder="1.0.0" />
                        </Form.Item>
                        <Form.Item label="Ghi chú phát hành" name="releaseNote">
                            <Input placeholder="Vd: Sửa lỗi đăng nhập..." />
                        </Form.Item>
                    </div>

                    <Form.Item label="Mô tả" name="description">
                        <Input.TextArea rows={3} />
                    </Form.Item>

                    <Form.Item
                        label="File cài đặt (.apk, .ipa, .exe)"
                        name="file"
                        valuePropName="fileList"
                        getValueFromEvent={normFile}
                        rules={[{ required: true, message: 'Bắt buộc phải có file' }]}
                    >
                        <Dragger name="file" maxCount={1} beforeUpload={() => false}>
                            <p className="ant-upload-drag-icon">
                                <InboxOutlined />
                            </p>
                            <p className="ant-upload-text">Kéo thả file vào đây hoặc bấm để chọn</p>
                            <p className="ant-upload-hint">Hỗ trợ file cài đặt dung lượng lớn</p>
                        </Dragger>
                    </Form.Item>

                    <Form.Item>
                        <Button type="primary" htmlType="submit" size="large" block loading={loading} icon={<UploadOutlined />}>
                            {loading ? 'Đang tải lên...' : 'Upload App'}
                        </Button>
                    </Form.Item>
                </Form>
            </Card>
        </div>
    );
};

export default UploadPage;