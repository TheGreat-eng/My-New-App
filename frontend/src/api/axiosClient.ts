import axios from "axios";



const axiosClient = axios.create({
    baseURL: 'http://localhost:8080/api',
    headers: {
        'Content-Type': 'application/json',
    },
});



axiosClient.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('accessToken');

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
            localStorage.removeItem('accessToken');
            window.location.href = '/login';
        }


        return Promise.reject(error);
    }
)

export default axiosClient;


