import type { IAppDTO, IAppVersion } from "../types"
import axiosClient from "./axiosClient"


export const appApi = {
    uploadApp: (data: FormData) => {
        return axiosClient.post<IAppVersion>('/apps/upload', data, {
            headers: {
                'Content-Type': 'multipart/form-data',
            }
        })
    },


    getAllApps: () => {
        return axiosClient.get<IAppDTO[]>('/apps');
    },
}