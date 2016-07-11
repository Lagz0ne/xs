# xs
The simplest IOC container for Java

## Pretro
As I discovered RxJava, I realized that Rx itself can cover most problems of building an Inversion of Control - then why shouldn't we utilize RxJava to do it? So, why not, bring it on.

## Plan
### IoC nature
- Inversion of control
- Singleton
- Prototype
- Lazy loading
- Say no to reflection

## Tasks

- [x] Generate constructable class
- [x] Integrate that with Annotation processor
- [x] Integrate generated classes with ServiceLoader
- [x] Network magic to have a proper IoC
- [ ] Everything should be tested by then
- [ ] Support interfaces and abstract classes
- [ ] Support Singleton and Prototype
