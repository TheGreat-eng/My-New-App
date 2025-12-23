import axiosClient from "./axiosClient"


interface LoginResponse {
    accessToken: string,
    tokenType: string,
}


export const authApi = {
    login: (data: any) => {
        return axiosClient.post<LoginResponse>('auth/login', data)
    },

    register: (data: any) => {
        return axiosClient.post('/auth/signup', data);
    },
}