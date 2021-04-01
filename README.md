### 操作环境
  - jdk: zulu_jdk_1.8.0_275
  - opencv: 4.5.1
  - ffmpeg: 4.3.1

### 注意事项：
  - 运行前需要将 `resources/lib` 目录下的lib文件根据自己的系统复制当前服务器下的 `java.library.path`下,程序运行时会打印当前服务器的 java.library.path，注意查看
    - windows 系统 为 `libopencv_java451.dll`，可直接下载 .exe 文件后在安装路径根据自己的系统位数查找(项目已提供)
    - mac 系统为 `libopencv_java451.dylib`，需要手动编译(项目已提供)
    - linux 系统为 `libopencv_java451.so`，需要手动编译(项目已提供)
  - 操作视频需要集成`ffmpeg`,如果是`mac/linux`系统需要先编译`ffmpeg`成功后再通过命令集成编译`ffmpeg`，`windows`系统直接下载`ffmpeg`即可
    > 理论上应该不需要用户再内置ffmpeg，如果出现通过opencv的工具类无法操作视频的情况，请考虑下载或编译ffmpeg