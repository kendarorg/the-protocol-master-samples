using Microsoft.AspNetCore.Cors;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using RestServer.EF;
using Task = RestServer.EF.Task;

namespace RestServer.Controllers
{
    [Route("api/tasks")]
    [ApiController]
    public class TaskController : ControllerBase
    {
        private readonly EFContext _context;

        public TaskController(EFContext context)
        {
            this._context = context;
        }

        // GET: api/tasks
        [HttpGet]
        public ActionResult<IEnumerable<Task>> GetTasks()
        {
            return  _context.Tasks.ToList();
        }

        // GET: api/tasks/5
        // <snippet_GetByID>
        [HttpGet("{id}")]
        public ActionResult<Task> GetTask(long id)
        {
            var Task =  _context.Tasks.Find(id);

            if (Task == null)
            {
                return NotFound();
            }

            return Task;
        }
        // </snippet_GetByID>

        // PUT: api/tasks/5
        // To protect from overposting attacks, see https://go.microsoft.com/fwlink/?linkid=2123754
        // <snippet_Update>
        [HttpPut("{id}")]
        public IActionResult PutTask(long id, UpdateTask TaskUpdate)
        {
            if (id != TaskUpdate.Id)
            {
                return BadRequest();
            }
            var Task = _context.Tasks.Find(id);
            Task.Priority = TaskUpdate.Priority;
            Task.Status = TaskUpdate.Status;

            _context.Entry(Task).State = EntityState.Modified;

            try
            {
                 _context.SaveChanges();
            }
            catch (DbUpdateConcurrencyException)
            {
                if (!TaskExists(id))
                {
                    return NotFound();
                }
                else
                {
                    throw;
                }
            }

            return NoContent();
        }
        // </snippet_Update>

        // POST: api/tasks
        // To protect from overposting attacks, see https://go.microsoft.com/fwlink/?linkid=2123754
        // <snippet_Create>
        [HttpPost]
        public ActionResult<Task> PostTask(Task Task)
        {
            _context.Tasks.Add(Task);
             _context.SaveChanges();

            //    return CreatedAtAction("GetTask", new { id = Task.Id }, Task);
            return CreatedAtAction(nameof(GetTask), new { id = Task.Id }, Task);
        }
        // </snippet_Create>

        // DELETE: api/tasks/5
        [HttpDelete("{id}")]
        public IActionResult DeleteTask(long id)
        {
            var Task =  _context.Tasks.Find(id);
            if (Task == null)
            {
                return NotFound();
            }

            _context.Tasks.Remove(Task);
             _context.SaveChanges();

            return NoContent();
        }

        private bool TaskExists(long id)
        {
            return _context.Tasks.Any(e => e.Id == id);
        }

        [HttpPut("archive/{id}")]
        public IActionResult ArchiveTask(long id)
        {
            var Task = _context.Tasks.Find(id);
            Task.Status = "Archived";
            Task.ArchiveDate = DateTime.Now;

            _context.Entry(Task).State = EntityState.Modified;

            try
            {
                _context.SaveChanges();
            }
            catch (DbUpdateConcurrencyException)
            {
                if (!TaskExists(id))
                {
                    return NotFound();
                }
                else
                {
                    throw;
                }
            }

            return NoContent();
        }
    }
}
