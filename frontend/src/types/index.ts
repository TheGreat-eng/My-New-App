export interface IApp {
    id: number,
    name: string,
    packageName: string,
    description: string,
    iconUrl?: string,
}


export interface IAppVersion {
    id: number,
    app: IApp,
    version: string,
    status: 'DRAFT' | 'PENDING' | 'PUBLISED',
    fileUrl: string,
    fileSize: number,
}




export interface IAppDTO {
    id: number;
    name: string;
    description: string;
    iconUrl?: string;
    latestVersion: string;
    downloadUrl: string;
    updatedAt: string;
}