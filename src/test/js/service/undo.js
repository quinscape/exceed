const assert = require("power-assert");
const Promise = require("es6-promise-polyfill").Promise;
const sinon = require("sinon");
const proxyquire = require("proxyquire");
const sys = require("../../../main/js/sys");
sys.init("/test-context", "TestApp");

const undoService = require("../../../main/js/service/undo");

describe("Undo", function(){
    let state = {
        value: null
    };

    let undo = undoService.create(state, (s, done) =>
    {
        state = s;
        done();
    });

	it("handles states", function()
	{
        assert( undo.isSaved());
        assert(!undo.canUndo());
        assert(!undo.canRedo());

        const  doneSpy = sinon.spy();
        undo.newState({
            value: "new"
        }, doneSpy);

        assert( state.value === "new");
        assert( !undo.isSaved());
        assert( undo.canUndo());
        assert(!undo.canRedo());

        assert(doneSpy.called);

        const doneSpy2 = sinon.spy();
        undo.undo(doneSpy2);

        assert( state.value === null);
        assert( undo.isSaved());
        assert( !undo.canUndo());
        assert(undo.canRedo());
        assert(doneSpy2.called);

        undo.newState({
            value: "new2"
        });

        const doneSpy3 = sinon.spy();
        undo.markSaved(doneSpy3);

        assert( state.value === "new2");
        assert( undo.isSaved());
        assert( undo.canUndo());
        assert(!undo.canRedo());
        assert(doneSpy3.called);

        undo.undo();

        assert( state.value === null);
        assert( !undo.isSaved());
        assert( !undo.canUndo());
        assert(undo.canRedo());

    });

});
