# React Guide

## 1. JSX

### 1.1 Embed Expressions in JSX

```
function formatName(user) {
    return user.firstName + ' ' + user.lastName
}
const user = {
    firstName: 'Harper',
    lastName: 'Perez'
};

const element = (
    <h1>
      Hello, {formatName(user)}!
    </h1>
); 

ReactDOM.render(
    element,
    document.getElementById('root')
)
```

- wrap JSX in parentheses to split line & avoid auto semicolon insertion

### 1.2 JSX is an Expression Too 

JSX become regular JS object after compilation
```
function getGreeting(user) {
  if (user) {
    return <h1>Hello, {formatName(user.name)}!</h1>;
  } else {
    return <h1>Hello, Stranger.</h1>;
  }
}
```


### 1.3 Specify attributes & children

```babel
const element = <div tabIndex="0"></div>;
const element = <img src={user.avatarUrl}></img>;
const element = <img src={user.avatarUrl} />;
const element = (
  <div>
    <h1>Hello!</h1>
    <h2>Good to see you here.</h2>
  </div>
);
```

## 2. Rendering Elements

Render & Update Elements

Timer Example
```
function tick() {
  const element = (
    <div>
      <h1>Hello, world!</h1>
      <h2>It is {new Date().toLocaleTimeString()}.</h2>
    </div>
  );
  ReactDOM.render(
    element,
    document.getElementById('root')
  );
}

setInterval(tick, 1000);
```

Even though we create an element describing the whole UI tree on every tick, only the text node whose contents has changed gets updated by React DOM.


## 3. Components and Props

### 3.1 Define a component with Function or ES6 class

```babel
function Welcome(props) {
  return <h1>Hello, {props.name}</h1>;
}

class Welcome extends React.Component {
  render() {
    return <h1>Hello, {this.props.name}</h1>;
  }
}
```

### 3.2 React can represent user-defined component

```
function Welcome(props) {
  return <h1>Hello, {props.name}</h1>;
}

function App() {
  return (
    <div>
      <Welcome name="Sara" />
      <Welcome name="Cahal" />
      <Welcome name="Edite" />
    </div>
  );
}

ReactDOM.render(
  <App />,
  document.getElementById('root')
);
```


### 3.3 Comment example
```
function formatDate(date) {
  return date.toLocaleDateString();
}

function Avatar(props) {
  return (
    <img className="Avatar"
         src={props.user.avatarUrl}
         alt={props.user.name} />
  );
}

function UserInfo(props) {
  return (
    <div className="UserInfo">
      <Avatar user={props.user} />
      <div className="UserInfo-name">
        {props.user.name}
      </div>
    </div>
  );
}

function Comment(props) {
  return (
    <div className="Comment">
      <UserInfo user={props.author} />
      <div className="Comment-text">
        {props.text}
      </div>
      <div className="Comment-date">
        {formatDate(props.date)}
      </div>
    </div>
  );
}

const comment = {
  date: new Date(),
  text: 'I hope you enjoy learning React!',
  author: {
    name: 'Hello Kitty',
    avatarUrl: 'http://placekitten.com/g/64/64'
  }
};

ReactDOM.render(
  <Comment
    date={comment.date}
    text={comment.text}
    author={comment.author} />,
  document.getElementById('root')
);
```

Notice: Props are read only

## 4. State and Lifecycle

Original clock
```
function Clock(props) {
  return (
    <div>
      <h1>Hello, world!</h1>
      <h2>It is {props.date.toLocaleTimeString()}.</h2>
    </div>
  );
}

function tick() {
  ReactDOM.render(
    <Clock date={new Date()} />,
    document.getElementById('root')
  );
}

setInterval(tick, 1000);
```

How to make a truly reusable clock?

### 4.1 Convert Clock function to a Class with local state

```
class Clock extends React.Component {
  constructor(props) {
    super(props);
    this.state = {date: new Date()};
  }

  render() {
    return (
      <div>
        <h1>Hello, world!</h1>
        <h2>It is {this.state.date.toLocaleTimeString()}.</h2>
      </div>
    );
  }
}

ReactDOM.render(
  <Clock />,
  document.getElementById('root')
);

```

Now it is just static

### 4.2 Adding lifecycle methods to a Class

- lifecycle hooks
    + componentDidMount: runs after the component output has been rendered to the DOM. This is a good place to set up a timer
    + componentWillUnmount

```
  componentDidMount() {
    this.timerID = setInterval(
      () => this.tick(),
      1000
    );
  }

  componentWillUnmount() {
    clearInterval(this.timerID);
  }

  tick() {
    this.setState({
      date: new Date()
    });
  }
```

### 4.3 Using State Correctly

- Do not modify state directly
    + like `this.state.comment = 'Hello'`
    + instead `this.setState({comment: 'Hello'})`
- State Update may be async
    + when using props and state together, use a function
    + Don't `this.setState({ counter: this.state.counter + this.props.increment });`
    + `this.setState( (prevState,props) => ({counter: prevState.counter + props.increment }) );`
- State updates are merged
    + React merges the object you provide into current state
- Data Flows Down
    + Neither parent nor child components can know if a certain component is stateful or stateless
    + This is a top-down data flow.
    + Three <Clock/> will set up own timer, updates independently

## 5. Handling Events

- syntactic difference
    + use camelCase rather than lowercase in DOM
    + with JSX you pass function as event handler rather than string
    + `onclick="activateLaser()"` => `onClick={activeLaser}`
- cannot return false to prevent default bahavior
    + call e.preventDefault() explicitly
    + `onclick="console.log('clicked'); return false"`
    + `onClick={e.preventDefault();console.log('clicked')}`
- EventHandler

```
class Toggle extends React.Component {
  constructor(props) {
    super(props);
    this.state = {isToggleOn: true};

    // This binding is necessary to make `this` work in the callback
    this.handleClick = this.handleClick.bind(this);
  }

  handleClick() {
    this.setState(prevState => ({
      isToggleOn: !prevState.isToggleOn
    }));
  }

  render() {
    return (
      <button onClick={this.handleClick}>
        {this.state.isToggleOn ? 'ON' : 'OFF'}
      </button>
    );
  }
}

ReactDOM.render(
  <Toggle />,
  document.getElementById('root')
);
```

In JavaScript, class methods are not bound by default. If you forget to bind this.handleClick and pass it to onClick, this will be undefined when the function is actually called.

- Solutions
    + fat Error notion
        * `handleClick = () => {}`
        * experimental syntax
    + bind in render view
        * `onClick={this.handleClick.bind(this)}`
    + change component Click
        * `onClick={(e) => this.handleClick(e)}`

## 6. Conditional Rendering

### 6.1 Element variable

```
render() {
    const isLoggedIn = this.state.isLoggedIn;

    let button = null;
    if (isLoggedIn) {
      button = <LogoutButton onClick={this.handleLogoutClick} />;
    } else {
      button = <LoginButton onClick={this.handleLoginClick} />;
    }

    return (
      <div>
        <Greeting isLoggedIn={isLoggedIn} />
        {button}
      </div>
    );
  }
```

### 6.2 Inline If with Logical && Operator
// conditional include a element

```
function Mailbox(props) {
  const unreadMessages = props.unreadMessages;
  return (
    <div>
      <h1>Hello!</h1>
      {unreadMessages.length > 0 &&
        <h2>
          You have {unreadMessages.length} unread messages.
        </h2>
      }
    </div>
  );
}

const messages = ['React', 'Re: React', 'Re:Re: React'];
ReactDOM.render(
  <Mailbox unreadMessages={messages} />,
  document.getElementById('root')
);
```

### 6.3 Inline If-Else with Conditional Operator

```
render() {
  const isLoggedIn = this.state.isLoggedIn;
  return (
    <div>
      The user is <b>{isLoggedIn ? 'currently' : 'not'}</b> logged in.
    </div>
  );
}
```

### 6.4 Preventing Component from Rendering

In a rare case, hide component even though it was rendered by another component

```
function WarningBanner(props) {
  if (!props.warn) {
    return null;
  }

  return (
    <div className="warning">
      Warning!
    </div>
  );
}

<WarningBanner warn={this.state.showWarning} />
```

## 7. Lists and Keys

### 7.1 Rendering multiple components

```
const numbers = [1,2,3,4,5]
const listItems = numbers.map((number)=>
    <li>{number}</li>
);
ReactDOM.render(
  <ul>{listItems}</ul>,
  document.getElementById('root')
);
```

### 7.2 Keys

a key should be provided for list items

Keys help React identify which items have changed, are added, or are removed. Keys should be given to the elements inside the array to give the elements a stable identity

```
function NumberList(props) {
  const numbers = props.numbers;
  const listItems = numbers.map((number) =>
    <li key={number.toString()}>
      {number}
    </li>
  );
  return (
    <ul>{listItems}</ul>
  );
}

const numbers = [1, 2, 3, 4, 5];
ReactDOM.render(
  <NumberList numbers={numbers} />,
  document.getElementById('root')
);
```


- Most often you would use IDs from your data as keys
- When you don't have stable IDs for rendered items, you may use the item index as a key as a last resort. We don't recommend using indexes for keys if the items can reorder.

```
const todoItems = todos.map((todo) =>
  <li key={todo.id}>
    {todo.text}
  </li>
);

const todoItems = todos.map((todo, index) =>
  // Only do this if items have no stable IDs
  <li key={index}>
    {todo.text}
  </li>
);
```

- Extracting Components with Keys 
    + if you extract a ListItem component, 
    + keep the key on the <ListItem /> elements in the array 
    + rather than on the root `<li>` element in the ListItem itself.
- Keys Must Only Be Unique Among Siblings
    + do not have to be globally unique

### 7.3 Embed in JSX

```
function NumberList(props) {
  const numbers = props.numbers;
  return (
    <ul>
      {numbers.map((number) =>
        <ListItem key={number.toString()}
                  value={number} />
      )}
    </ul>
  );
}
```

## 8. Forms

### 8.1 Basic Text Input
```
class Form extends React.Component {
  constructor(props) {
    super(props);
    this.state = {value: ""};
    this.handleChange = this.handleChange.bind(this);
    this.handleSubmit = this.handleSubmit.bind(this);
  }

  handleChange(event) {
    this.setState({value: event.target.value});
  }

  handleSubmit(event) {
    alert("Text field value is: '" + this.state.value + "'");
  }

  render() {
    return (
      <div>
        <input type="text" placeholder="edit me"
          value={this.state.value} onChange={this.handleChange} />
        <button onClick={this.handleSubmit}>Submit</button>
      </div>
    );
  }
}

ReactDOM.render(<Form />, document.getElementById('root'));
```

### 8.2 Basic TextArea
```
class Form extends React.Component {
  constructor(props) {
    super(props);
    this.state = {value: ""};
    this.handleChange = this.handleChange.bind(this);
    this.handleSubmit = this.handleSubmit.bind(this);
  }

  handleChange(event) {
    this.setState({value: event.target.value});
  }

  handleSubmit(event) {
    alert("Textarea value is: '" + this.state.value + "'");
  }

  render() {
    return (
      <div>
        <textarea
          name="description"
          value={this.state.value}
          onChange={this.handleChange}
        />
        <button onClick={this.handleSubmit}>Submit</button>
      </div>
    );
  }
}

ReactDOM.render(<Form />, document.getElementById('root'));
```

### 8.3 Basic Select
```
class Form extends React.Component {
  constructor(props) {
    super(props);
    this.state = {value: "B"};
    this.handleChange = this.handleChange.bind(this);
    this.handleSubmit = this.handleSubmit.bind(this);
  }

  handleChange(event) {
    this.setState({value: event.target.value});
  }

  handleSubmit(event) {
    alert("Select value is: '" + this.state.value + "'");
  }

  render() {
    return (
      <div>
        <select value={this.state.value} onChange={this.handleChange}>
          <option value="A">Apple</option>
          <option value="B">Banana</option>
          <option value="C">Cranberry</option>
        </select>
        <button onClick={this.handleSubmit}>Submit</button>
      </div>
    );
  }
}

ReactDOM.render(<Form />, document.getElementById('root'));
```

### 8.4 Basic Radio Button
```
class Form extends React.Component {
  constructor(props) {
    super(props);
    this.state = {value: "B"};
    this.handleChange = this.handleChange.bind(this);
    this.handleSubmit = this.handleSubmit.bind(this);
  }

  handleChange(event) {
    this.setState({value: event.target.value});
  }

  handleSubmit(event) {
    alert("Radio button value is: '" + this.state.value + "'");
  }

  render() {
    return (
      <div>
        <input type="radio" name="choice" value="A" onChange={this.handleChange} /> Option A<br />
        <input type="radio" name="choice" value="B" onChange={this.handleChange} defaultChecked={true} /> Option B<br />
        <input type="radio" name="choice" value="C" onChange={this.handleChange} /> Option C<br />
        <br />
        <button onClick={this.handleSubmit}>Submit</button>
      </div>
    );
  }
}

ReactDOM.render(<Form />, document.getElementById('root'));
```

### 8.5 Basic Checkbox
```
class Form extends React.Component {
  constructor(props) {
    super(props);
    this.state = {checked: ["B"]};
    this.handleChange = this.handleChange.bind(this);
    this.handleSubmit = this.handleSubmit.bind(this);
  }

  handleChange(event) {
    let val = event.target.value;
    let checked = this.state.checked.slice(); // copy
    if(checked.includes(val)) {
      checked.splice(checked.indexOf(val), 1);
    } else {
      checked.push(val);
    }
    this.setState({checked: checked})
  }

  handleSubmit(event) {
    alert("Boxes checked are: '" + this.state.checked + "'");
  }

  render() {
    return (
      <div>
        <input type="checkbox" value="A" onChange={this.handleChange} /> Option A<br />
        <input type="checkbox" value="B" onChange={this.handleChange} defaultChecked={true} /> Option B<br />
        <input type="checkbox" value="C" onChange={this.handleChange} /> Option C<br />
        <br />
        <button onClick={this.handleSubmit}>Submit</button>
      </div>
    );
  }
}
ReactDOM.render(<Form />, document.getElementById('root'));
```

## 9. Lifting state up

Often, several components need to reflect the same changing data. We recommend lifting the shared state up to their closest common ancestor. 

a component accept temperature => whether boiled
```
function BoilingVerdict(props) {
  if (props.celsius >= 100) {
    return <p>The water would boil.</p>;
  } else {
    return <p>The water would not boil.</p>;
  }
}
```

a Calculator renders input
```
class Calculator extends React.Component {
  constructor(props) {
    super(props);
    this.state = {value: ''};
  }

  handleChange = (e) => {
    this.setState({value: e.target.value});
  }

  render() {
    const value = this.state.value;
    return (
      <fieldset>
        <legend>Enter temperature in Celsius:</legend>
        <input value={value} onChange={this.handleChange} />
        <BoilingVerdict celsius={parseFloat(value)} />
      </fieldset>
    );
  }
}
```

### 9.1 Celsius/Fahrenheit
```
const scaleNames = {
  c: 'Celsius',
  f: 'Fahrenheit'
};

class TemperatureInput extends React.Component {
  constructor(props) {
    super(props);
    this.handleChange = this.handleChange.bind(this);
    this.state = {value: ''};
  }

  handleChange(e) {
    this.setState({value: e.target.value});
  }

  render() {
    const value = this.state.value;
    const scale = this.props.scale;
    return (
      <fieldset>
        <legend>Enter temperature in {scaleNames[scale]}:</legend>
        <input value={value}
               onChange={this.handleChange} />
      </fieldset>
    );
  }
}

class Calculator extends React.Component {
  render() {
    return (
      <div>
        <TemperatureInput scale="c" />
        <TemperatureInput scale="f" />
      </div>
    );
  }
}

```

### 9.2 Lifting state up

Converter
```
function toCelsius(fahrenheit) {
  return (fahrenheit - 32) * 5 / 9;
}

function toFahrenheit(celsius) {
  return (celsius * 9 / 5) + 32;
}

function tryConvert(value, convert) {
  const input = parseFloat(value);
  if (Number.isNaN(input)) {
    return '';
  }
  const output = convert(input);
  const rounded = Math.round(output * 1000) / 1000;
  return rounded.toString();
}
```

Let TemperatureIntput accept both `value` and `onChange` as prop now

```
class TemperatureInput extends React.Component {
  constructor(props) {
    super(props);
    this.handleChange = this.handleChange.bind(this);
  }

  handleChange(e) {
    this.props.onChange(e.target.value);
  }

  render() {
    const value = this.props.value;
    const scale = this.props.scale;
    return (
      <fieldset>
        <legend>Enter temperature in {scaleNames[scale]}:</legend>
        <input value={value}
               onChange={this.handleChange} />
      </fieldset>
    );
  }
}
```

We could have stored the value of both inputs but it turns out to be unnecessary. It is enough to store the value of the most recently changed input, and the scale that it represents. We can then infer the value of the other input based on the current value and scale alone.

The inputs stay in sync because their values are computed from the same state:

```
class Calculator extends React.Component {
  constructor(props) {
    super(props);
    this.handleCelsiusChange = this.handleCelsiusChange.bind(this);
    this.handleFahrenheitChange = this.handleFahrenheitChange.bind(this);
    this.state = {value: '', scale: 'c'};
  }

  handleCelsiusChange(value) {
    this.setState({scale: 'c', value});
  }

  handleFahrenheitChange(value) {
    this.setState({scale: 'f', value});
  }

  render() {
    const scale = this.state.scale;
    const value = this.state.value;
    const celsius = scale === 'f' ? tryConvert(value, toCelsius) : value;
    const fahrenheit = scale === 'c' ? tryConvert(value, toFahrenheit) : value;

    return (
      <div>
        <TemperatureInput scale="c" value={celsius} onChange={this.handleCelsiusChange} />
        <TemperatureInput scale="f" value={fahrenheit} onChange={this.handleFahrenheitChange} />
        <BoilingVerdict celsius={parseFloat(celsius)} />
      </div>
    );
  }
}
```

## 10. Composition vs Inheritance

React prefer composition over inheritance

### 10.1 Containment

- Some components don't know their children ahead of time
- common for boxes like Sidebar or Dialog
    + recommend use special children prop to pass directly to output
```
function FancyBorder(props) {
  return (
    <div className={'FancyBorder FancyBorder-' + props.color}>
      {props.children}
    </div>
  );
}
```

this allows other component pass arbitrary children
```
function WelcomeDialog() {
  return (
    <FancyBorder color="blue">
      <h1 className="Dialog-title">
        Welcome
      </h1>
      <p className="Dialog-message">
        Thank you for visiting our spacecraft!
      </p>
    </FancyBorder>
  );
}
```

in a rare case, multiple hole container
```
function SplitPane(props) {
  return (
    <div className="SplitPane">
      <div className="SplitPane-left">
        {props.left}
      </div>
      <div className="SplitPane-right">
        {props.right}
      </div>
    </div>
  );
}

function App() {
  return (
    <SplitPane
      left={
        <Contacts />
      }
      right={
        <Chat />
      } />
  );
}
```

### 10.2 Specialization

 WelcomeDialog is a special case of Dialog.
 Config the props instead of inherit

 ```
 function Dialog(props) {
  return (
    <FancyBorder color="blue">
      <h1 className="Dialog-title">
        {props.title}
      </h1>
      <p className="Dialog-message">
        {props.message}
      </p>
    </FancyBorder>
  );
}

function WelcomeDialog() {
  return (
    <Dialog
      title="Welcome"
      message="Thank you for visiting our spacecraft!" />
  );
}
 ```

also works for class components
```
function Dialog(props) {
  return (
    <FancyBorder color="blue">
      <h1 className="Dialog-title">{props.title}</h1>
      <p className="Dialog-message">{props.message}</p>
      {props.children}
    </FancyBorder>
  );
}

class SignUpDialog extends React.Component {
  constructor(props) {
    super(props);
    this.handleChange = this.handleChange.bind(this);
    this.handleSignUp = this.handleSignUp.bind(this);
    this.state = {login: ''};
  }

  render() {
    return (
      <Dialog title="Mars Exploration Program" message="How should we refer to you?">
        <input value={this.state.login}onChange={this.handleChange} />
        <button onClick={this.handleSignUp}>Sign Me Up!</button>
      </Dialog>
    );
  }

  handleChange(e) {
    this.setState({login: e.target.value});
  }

  handleSignUp() {
    alert(`Welcome aboard, ${this.state.login}!`);
  }
}
```

