import axios from "axios";



const axiosClient = axios.create({
    baseURL: 'http://localhost:8080/api',
    headers: {
        'Content-Type': 'application/json',
    },
});



axiosClient.interceptors.request.use(
    (config) => {
        // Lấy token từ LocalStorage
        const token = localStorage.getItem('accessToken');

        if (token) {
            // Kẹp vào header: Authorization: Bearer eyJhbGci...
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    (error) => Promise.reject(error)
);


axiosClient.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response?.status === 401) {
            localStorage.removeItem('accessToken');
            window.location.href = '/login';
        }


        return Promise.reject(error);
    }
)

export default axiosClient;


