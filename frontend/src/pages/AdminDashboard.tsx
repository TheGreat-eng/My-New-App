import { CheckOutlined, CloseOutlined } from "@ant-design/icons";
import { Button, Card, message, Space, Table, Tag } from "antd";
import { useEffect, useState } from "react"
import axiosClient from "../api/axiosClient";

const AdminDashboard = () => {
    const [apps, setApps] = useState([]);
    const [loading, setLoading] = useState();



    const fetchPendingApps = async () => {
        try {
            const res = await axiosClient.get('/apps/pending');
            setApps(res.data);
        } catch (error) {
            console.error(error)
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchPendingApps();
    }, []);


    const handleApprove = async (id: number, isApproved: boolean) => {
        try {
            await axiosClient.post(`/apps/${id}/approve?isApproved=${isApproved}`);
            message.success(isApproved ? 'Đã duyệt ứng dụng!' : 'Đã từ chối ứng dụng!');
            fetchPendingApps(); // Reload lại bảng
        } catch (error) {
            message.error('Lỗi khi xử lý');
        }
    }

    const columns = [
        { title: 'Tên App', dataIndex: 'name', key: 'name' },
        { title: 'Phiên bản', dataIndex: 'latestVersion', key: 'version' },
        {
            title: 'Trạng thái',
            key: 'status',
            render: () => <Tag color="orange">Chờ duyệt</Tag>
        },
        { title: 'Người đăng', dataIndex: 'createdBy', key: 'createdBy' }, // Cần backend trả về field này
        {
            title: 'Hành động',
            key: 'action',
            render: (_: any, record: any) => (
                <Space>
                    <Button
                        type="primary"
                        icon={<CheckOutlined />}
                        style={{ backgroundColor: '#52c41a' }}
                        onClick={() => handleApprove(record.id, true)}
                    >
                        Duyệt
                    </Button>
                    <Button
                        danger
                        icon={<CloseOutlined />}
                        onClick={() => handleApprove(record.id, false)}
                    >
                        Từ chối
                    </Button>
                </Space>
            ),
        },
    ];


    return (
        <div style={{ padding: 20 }}>
            <Card title="Danh sach ung dung cho phe duyet">
                <Table dataSource={apps} columns={columns} loading={loading}
                    rowKey="id" />

            </Card>
        </div>
    )

}


export default AdminDashboard