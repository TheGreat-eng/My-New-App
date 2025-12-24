import axios from "axios";
import { User } from "oidc-client-ts";

// ✅ Tạo function helper để lấy token
const getAccessToken = (): string | null => {
    // Lấy từ sessionStorage (nơi react-oidc-context lưu)
    const oidcStorage = sessionStorage.getItem(`oidc.user:http://localhost:8081/realms/enterprise-realm:eas-client`);
    if (oidcStorage) {
        const user: User = JSON.parse(oidcStorage);
        return user.access_token;
    }
    // Fallback to localStorage (nếu bạn vẫn muốn giữ)
    return localStorage.getItem('accessToken');
};

const axiosClient = axios.create({
    baseURL: 'http://localhost:8080/api',
    headers: {
        'Content-Type': 'application/json',
    },
});

axiosClient.interceptors.request.use(
    (config) => {
        const token = getAccessToken(); // ✅ Dùng helper

        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }

        // QUAN TRỌNG: Không set Content-Type cho multipart (để browser tự động set với boundary)
        if (config.data instanceof FormData) {
            delete config.headers['Content-Type'];
        }

        return config;
    },
    (error) => Promise.reject(error)
);


axiosClient.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response?.status === 401) {
            // ✅ Xóa cả 2 nơi lưu token
            localStorage.removeItem('accessToken');
            sessionStorage.clear();
            window.location.href = '/login';
        }


        return Promise.reject(error);
    }
)

export default axiosClient;


