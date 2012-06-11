One may ask how to integrate the code of gaedo in your application, especially in the case of applications making use of !IoC containers. We don't have replies for everything, but for Guice, we have ! There is a new module in gaedo called (with a remarkable imagination) `gaedo-guice`. This module contains a simple class, `GaedoModule`. When using this module in yout application, you gain all mandatory context elements :
 
   * `ServiceRepository` 
   * `ServiceGenerator`
   * `PropertyLocator` 
   * `FieldLocator`

All are provided as elements of that `GaedoModule`. using it, you only have to write your own services implementations and put them in the repository to have working code. To integrate it, well, as usual, you have to take the maven way.