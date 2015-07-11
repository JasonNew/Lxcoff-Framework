# Lxc-offloading
mobile offloading with lxc cloud support

## Mobile offloading
>Mobile offloading is a key concept in mobile cloud. It means mobile apps can offload computation-intensive code to cloud to use the computing power supplied by cloud infrastructure.

It's different from traditional client-server app solution because in mobile offloading developers don't need to develop the server side at all! All the computation logic is in the app. In this case, the app can decide whether the computation should go to cloud according to the context in the mobile device, such as power and network latency.

If you wannna know more about mobile offloading, you can google it.

## About Android container
In our offloading framework, the cloud runtime is not VM or JVM. We use os-level virtualization "linux container" as the runtime for mobile code. For the purpose of running android code in x86 GNU-Linux server, we modified android source code and the linux kernel it uses. The modification work is based on Android-x86 project. With our effort, android os can finally run in the ordinary linux containers!

## Offloading Framework
For our lxc-based offloading solution, we developed a offloading framework. Any application that follows this framework can use the mobile cloud power without any server side development. 



