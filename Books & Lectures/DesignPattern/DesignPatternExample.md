# Design Pattern Example

Case Study: Document Editor, Lexi

Seven Problems

- Document Structure: all editing, formatting, displaying will require traversing the representation
- Formatting: how object & representation interact will format policy
- User Interface: config without affect rest application
- support multiple look-and-feel standards
- support multiple window system
- user operation: button & menu function scattered through objects
- spelling check: how to support analytical operation

## 1. Document Structure

- Document: a arrangement of graphical elements
    + character, lines, polygons and other shapes
    + lines, columns, figures, tables, substructures
- Internal representation
    + should support
        * maintain physical substructure
        * generating and presenting document
        * map position on display to elements
    + with constraints
        * treat text and graphics uniformly
        * should not distinguish between single & group elements, to support arbitrarily complex document
        * need to analyze text for spelling & hyphenation, to ignore structural complexity (oppose to above constraint)
- Recursive Composition
    + requirement: object need corresponding class, class need compatible interface to be treated uniformly

### Glyph abstract class

Class Hierarchy

- Glyph: Draw(Window), Intersects(Point), Insert(Glyph,int)
    + Character 
        * Draw(Window w): w->DrawCharacter(c) 
        * Intersects(Point p): return true if point intersect this character
        * char c
    + Rectangle / Polygon
        * Draw()
        * Intersects()
    + Row
        * Draw(Window w) 
            - for all c in children:
            - if c positioned correctly: c->Draw()
        * Intersects(Point p)
            - for all c in children:
            - if c->intersects(p): return true
        * Insert(Glyph g, int i)
            - insert g into children at position i

Glyph Interface
```c++
virtual void Draw(Window*){}
virtual void Bounds(Rect&){}
virtual bool Intersects(const Point&){}
virtual void Insert(Glyph*, int){}
virtual void Remove(Glyph*){}
virtual Glyph* Child(int){}
virtual Glyph* Parent(){}
```

Glyph should know
- how to draw themselves
    + Subclass redefine draw
    + Rectangle: `Draw(Window){w->DrawRect(_x0,_y0,_x1,_y1)}`
- what space they occupy
    + Intersects return whether specified point 
    + respond when user clicked
- children and parent
    + Child operation return child at given index

> Composite Pattern: recursive composition in object-oriented terms


---

## 2. Formatting

formatting mean breaking a collection of glyphs into line, design a separate class hierarchy for objects that encapsulate formatting algorithm

### Compositor and Composition

Compositor interface
```c++
void SetComposition(Composition*){}
virtual void Compose(){}
```

Composition & Compositor class

- Glyph
    + Composition : Insert(Glyph g, int i)
        * Glyph::Insert(g,i)
        * compositor.Compose()
- Compositor
    + method: Compose(), SetComposition()
    + child
        * ArrayCompositor:Compose()
        * TeXCompositor:Compose()
        * SimpleCompositor:Compose()

Example

- A unformatted Composition object 
    + contains only visible glyph make up the basic content
- when need formatting
    + call compositor's Compose() operation
    + compositor iterates through composition children
    + insert new Row & Column glyph according to linebreaking algorithm
- compositor generated row/column glyphs
    + this split ensures separation between physical and formatting code
    + can add new Compositor without touching main glyph

> Strategy Pattern: Encapsulating an algorithm in an object. The key is Strategy object (Compositor) and the context that they operate (Composition). Design general Strategy interface to support a range of algorithm.

## 3. Embellish User Interface

two embellishments
- border around text editing area
- add scroll bar to view different parts

### Transparent Enclosure

- Use inheritance?
    + add subclass to Composition
        * BorderedComposition, 
        * ScrollableComposition, 
        * BorderedScrollableComposition
    + what if more combination of embellishment?
    + unworkable with the variety grows
- make embellishment itself as object
    + Border class
        * border in glyph, or glyph in border?
        * border have an appearance => subclass of Glyph
            - client should not care whether Glyph have borders
            - should treat them uniformly
    + transparent enclosure
        * single child composition + compatible interface
        * enclosure add state to component

### Monoglyph

- subclass of Glyph, for embellishment usage
- store a reference to component and forwards all request
- total transparent

Structure

- Glyph: Draw(Window)
    + MonoGlyph: `Draw(Window* w) {_component->Draw(w)}`
        * Border: `Draw(Window* w){MonoGlyph::Draw(w); DrawBorder(w);}`
        * Scroller

MonoGlyph subclass reimplement Draw, first invoke parent Draw to draw everything except the border, then call DrawBorder. Extends parent class instead of replacing it.

Border-Scroller-Composition-Column-Row-Elements

>Decorator Pattern: capture class & object relationship that support embellishment, anything add responsibility to object

## 4. Multiple look-and-feel standard

### Abstracting Object Creation

two set widget class
- a set of abstract Glyph subclasses for each category
    + ScrollBar, Button
- a set of concrete subclasses for each abstract subclass
    + MotifScrollBar, PMScrollBar

### Factory & Product Classes

Create instance via factory

    ScrollBar* sb = guiFactory->CreateScrollBar()


Hierarchy

- GUIFactory: CreateScrollBar(),CreateButton(),CreateMenu()
    + MotifFactory
    + PMFactory
    + MacFactory
- Glyph
    + ScrollBar:ScrollTo(int)
        * MotifScrollBar
        * MacScrollBar
        * PMScrollBar
    + Button:Press()
        * ...
    + Menu:Popup()
        * ...

Where do guiFactory variable come from?

- Anywhere that is convenient
    + global static member of a well-known class
    + Singleton pattern to manage such object
- as long as initialized before ever used

```
    GUIFactory* guiFactory;
    const char* styleName = getenv("LOOK_AND_FEEL");
        // user or environment supplies this at startup
    
    if (strcmp(styleName, "Motif") == 0) {
        guiFactory = new MotifFactory;
    
    } else if (strcmp(styleName, "Presentation_Manager") == 0) {
        guiFactory = new PMFactory;
    
    } else {
        guiFactory = new DefaultGUIFactory;
    }
```

> Abstract Factory Pattern: factory and product, how to create families of related product objects without instantiating classes directly. Used when have specific product families


## 5. Supporting multiple window system

### Encapsulating implementation dependencies

- Window System
    + provide oepration for drawing geometric shapes
    + can iconify / deiconify themselves
    + can resize themselves
    + can draw contents on demand (overlapped)
- extreme philosophy
    + intersection of functionality: cannot take more advanced feature if some window systems support them
    + union of functionality: may be huge and incoherent

Window Abstract Class Interface
```c++
virtual void Redraw(){}
virtual void Raise(){}
virtual void Lower(){}
virtual void Iconify(){}
virtual void Deiconify(){}

virtual void DrawLine(){}
virtual void DrawRect(){}
virtual void DrawPolygon(){}
virtual void DrawText(){}
```

Concrete window class support different windows user deal with, like application window, icon, warning dialog with different bahavior

WindowImp class to hide window system implementation, encapsulate system -dependent code

Hierarchy
- Window
    + member variable: `WindowImp _imp`
    + subclass
        * ApplicationWindow
        * IconWIndow: iconify()
        * DialogWindow
           - Window owner=Window
           - Lower() {owner.lower()}
- WindowImp
    + method: DeviceRaise(), DeviceRect()
    + subclass
        * MacWindowImp
        * PMWindowImp
        * XWindowImp

Drawing Method
```java
void Window::DrawRect (Coord x0, Coord y0, Coord x1, Coord y1) {
     _imp->DeviceRect(x0, y0, x1, y1);
}

// X has an operation for drawing rectangles explicitly 
void XWindowImp::DeviceRect (Coord x0, Coord y0, Coord x1, Coord y1) {
    int x = round(min(x0, x1));
    int y = round(min(y0, y1));
    int w = round(abs(x0 - x1));
    int h = round(abs(y0 - y1));
    XDrawRectangle(_dpy, _winid, _gc, x, y, w, h);
}

// PM has a more general interface for specifying vertices of multisegment shapes (called a path) and for outlining or filling the area they enclose.
void PMWindowImp::DeviceRect (Coord x0, Coord y0, Coord x1, Coord y1) {
    Coord left = min(x0, x1);
    Coord right = max(x0, x1);
    Coord bottom = min(y0, y1);
    Coord top = max(y0, y1);

    PPOINTL point[4];

    point[0].x = left;    point[0].y = top;
    point[1].x = right;   point[1].y = top;
    point[2].x = right;   point[2].y = bottom;
    point[3].x = left;    point[3].y = bottom;

    if (
        (GpiBeginPath(_hps, 1L) == false) ||
        (GpiSetCurrentPosition(_hps, &point[3]) == false) ||
        (GpiPolyLine(_hps, 4L, point) == GPI_ERROR)  ||
        (GpiEndPath(_hps) == false)
    ) {
        // report error

    } else {
        GpiStrokePath(_hps, 1L, 0L);
    }
}
```

Configure windows with windowimps
- use abstract factory
```java
    class WindowSystemFactory {
    public:
        virtual WindowImp* CreateWindowImp() = 0;
        virtual ColorImp* CreateColorImp() = 0;
        virtual FontImp* CreateFontImp() = 0;
        // a Create operation for all window system resources
    };

    //concrete class
    class PMWindowSystemFactory : public WindowSystemFactory {
        virtual WindowImp* CreateWindowImp()
            { return new PMWindowImp; }
    };
    
    class XWindowSystemFactory : public WindowSystemFactory {
        virtual WindowImp* CreateWindowImp()
            { return new XWindowImp; }
    };

    // Usage in window
    Window::Window () {
        _imp = windowSystemFactory->CreateWindowImp();
    }
```

> Bridge Pattern: application programmer won't deal with WindowImp interface directly, only deal with windows. Window Imp interface can more closely reflect system provide. Relation between them is a Bridge. Allow separate class hierarchy work together.


## 6. User Operation

- User operations
    + new/open/save/print document
    + cut/paste/select text
    + change font, style, format, alignment
    + quit application
- multiple interface for the same operation
    + turn page by button or option
- support a limited level of redo and undo

### Encapsulating a request

- can we use MenuItem as instance of a Glyph subclass?
    + it does not address undo/redo problem
    + hard to associate state with function
    + function is hard to reuse and extend
- should parameterize MenuItem with an object
    + not a function

### Command Class and Subclasses

Class Hierarchy

- Command: Execute()
    + PasteCommand
        * buffer
        * Execute(): paste buffer into document
    + Font Command
        * newFont
        * Execute(): make selected text appear in newFont
    + Save Command
        * Execute()： pop up a dialog to name the document & save
    + Quit Command
        * if modified, save->Execute()
        * quit application    
- Glyph
    + MenuItem
        * Click(): command->Execute()
        * Command command = new PasteCommand;
- Command History
    + a list of commands, with a present line
    + Execute() & Unexecute()

> Command Pattern: describe how to capsulate a request. Provide uniform interface for issuing request that let you configure clients to handle different requests

## 7. Spelling Checking and Hyphenation

### Encapsulating Access and Traversal

- Currently   
    + use integer index to refer children
    + may be inefficient for glyph that use linked list
    + should support different traverse at the same time
- add abstract operation to Glyph interface
    + void First(Traversal kind)
    + void Next()
    + bool IsDone()
    + Glyph* GetCurrent()
    + void Insert(Glyph*)
- problem
    + if put traverse logic inside glyph class
    + cannot support new traversal without extending
    + hard to modify or extends

### Iterator Class & Subclasses

- Glyph
    + CreateIterator(): return new NullIterator
    + subclass: return corresponding Iterator
- Iterator
    + method: First(), Next(), IsDone(), CurrentItem()
    + subclass
        * PreorderIterator
        * ArrayIterator
        * ListIterator
        * NullIterator

access childeren without knowing representation
```
    Glyph* g;
    Iterator<Glyph*>* i = g->CreateIterator();
    
    for (i->First(); !i->IsDone(); i->Next() ){
        Glyph* child = i->CurrentItem();
    }
```

Glyph Subclasses
```cpp
    Iterator<Glyph*>* Row::CreateIterator () {
        return new ListIterator<Glyph*>(_children);
    }
```
